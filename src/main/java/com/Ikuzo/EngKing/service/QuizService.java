package com.Ikuzo.EngKing.service;

import com.Ikuzo.EngKing.dto.QuestionResponseDto;
import com.Ikuzo.EngKing.entity.ChatRoom;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class QuizService {

    private final DynamoDbClient dynamoDbClient;
    private final RestTemplate restTemplate;

    @Autowired
    public QuizService(DynamoDbClient dynamoDbClient, RestTemplate restTemplate) {
        this.dynamoDbClient = dynamoDbClient;
        this.restTemplate = restTemplate;
    }

    public QuestionResponseDto createQuizRoom(String memberId, String quiz_type, String difficulty) {
        LocalDateTime rightNow = LocalDateTime.now().withNano(0);
        String chatRoomId = memberId + "_" + rightNow.toString();

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(chatRoomId); // DynamoDB의 HashKey로 사용
        chatRoom.setMemberId(memberId); // DynamoDB의 RangeKey로 사용
        chatRoom.setQuiz_type(quiz_type);
        chatRoom.setDifficulty(difficulty);
        chatRoom.setCreatedTime(rightNow);
        // DynamoDB에 ChatRoom 저장
        boolean dynamoDBResponse = saveChatRoomToDynamoDB(chatRoom);
        // 컨트롤러에 반환하는 객체
        QuestionResponseDto questionResponseDto = new QuestionResponseDto();
        questionResponseDto.setChatRoomId(chatRoomId);
        questionResponseDto.setMemberId(memberId);
        questionResponseDto.setCreatedTime(rightNow);
        questionResponseDto.setSuccess(dynamoDBResponse);

        return questionResponseDto;
    }

    // 채팅방 생성을 DynamoDB에 저장
    private boolean saveChatRoomToDynamoDB(ChatRoom chatRoom) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ChatRoomId", AttributeValue.builder().s(chatRoom.getChatRoomId()).build());
        item.put("MemberId", AttributeValue.builder().s(chatRoom.getMemberId()).build());
        item.put("Difficulty", AttributeValue.builder().s(chatRoom.getDifficulty()).build());
        item.put("Quiz_type", AttributeValue.builder().s(chatRoom.getQuiz_type()).build());
        item.put("CreatedTime", AttributeValue.builder().s(ChatRoom.LocalDateTimeConverter.convert(chatRoom.getCreatedTime())).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName("EngKing-ChatRoom")
                .item(item)
                .build();

        try {
            PutItemResponse response = dynamoDbClient.putItem(request);
            // 예외가 발생하지 않으면 저장이 성공
            return true;
        } catch (Exception e) {
            // 예외가 발생하면 저장에 실패
            System.err.println("Failed to save chat room to DynamoDB: " + e.getMessage());
            return false;
        }
    }

    // 답변 전송 및 다음 질문 요청
    public String createQuiz(String memberId, String chatRoomId, String input, String difficulty, String quiz_type, Boolean first) {

        String url = "http://13.231.43.88:5000/quiz";
        String requestBody = String.format("{\"user_id\": \"%s\", \"conversation_id\": \"%s\", \"input\": \"%s\", \"difficulty\": \"%s\", \"quiz_type\": \"%s\", \"first\": \"%s\"}",
                memberId, chatRoomId, input, difficulty, quiz_type, first);

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HTTP 엔터티 생성
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            // POST 요청 보내기
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 응답 상태 코드 확인
            if (response.getStatusCode() == HttpStatus.OK) {
                // 응답 본문을 JSON으로 변환
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.getBody());

                // 응답에서 필요한 정보 추출
                String content = jsonResponse.get("content").asText();

                return content;
            } else {
                // 오류 응답 처리
                throw new RuntimeException("Failed to get a valid response. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
            return null;
        }
    }

    // 채팅 메세지 내역 저장
    public boolean saveChatMessageToDynamoDB(String chatRoomId, String messageTime, String messageId, String senderId, String messageText, String audioFileUrl) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("ChatRoomId", AttributeValue.builder().s(chatRoomId).build());
        item.put("MessageTime", AttributeValue.builder().s(messageTime).build());
        item.put("MessageId", AttributeValue.builder().s(messageId).build());
        item.put("SenderId", AttributeValue.builder().s(senderId).build());
        item.put("MessageText", AttributeValue.builder().s(messageText).build());

        // Only add the AudioFileUrl if it is not null
        if (audioFileUrl != null) {
            item.put("AudioFileUrl", AttributeValue.builder().s(audioFileUrl).build());
        }

        PutItemRequest request = PutItemRequest.builder()
                .tableName("EngKing-ChatMessages")
                .item(item)
                .build();

        try {
            dynamoDbClient.putItem(request);
            // If no exception is thrown, saving is successful
            return true;
        } catch (Exception e) {
            // If an exception is thrown, saving failed
            System.err.println("Failed to save chat message to DynamoDB: " + e.getMessage());
            return false;
        }
    }



    // 대화 종료
    public QuestionResponseDto endQuiz(String memberId, String chatRoomId) {
        String url = "http://13.231.43.88:5000/quiz/evaluate";
        String requestBody = String.format("{\"user_id\": \"%s\", \"conversation_id\": \"%s\"}",
                memberId, chatRoomId);

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HTTP 엔터티 생성
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        try {
            // POST 요청 보내기
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 응답 상태 코드 확인
            if (response.getStatusCode() == HttpStatus.OK) {
                // 응답 본문을 JSON으로 변환
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.getBody());

                // 응답에서 필요한 정보 추출
                String score = jsonResponse.get("score").asText();
                String feedback = jsonResponse.get("feedback").asText();

                QuestionResponseDto questionResponseDto = new QuestionResponseDto();
                questionResponseDto.setScore(score);
                questionResponseDto.setFeedback(feedback);

                return questionResponseDto;
            } else {
                // 오류 응답 처리
                throw new RuntimeException("Failed to get a valid response. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // 예외 처리
            e.printStackTrace();
            return null;
        }
    }

    // DynamoDB에 채팅방 테의블의 score, feedback 부분 업데이트
    public boolean updateChatRoomScoreAndFeedback(String chatRoomId, String memberId, String score, String feedback) {
        try {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("ChatRoomId", AttributeValue.builder().s(chatRoomId).build());
            key.put("MemberId", AttributeValue.builder().s(memberId).build());

            Map<String, AttributeValueUpdate> updates = new HashMap<>();
            updates.put("Score", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(score).build())
                    .action(AttributeAction.PUT)
                    .build());
            updates.put("Feedback", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(feedback).build())
                    .action(AttributeAction.PUT)
                    .build());

            UpdateItemRequest request = UpdateItemRequest.builder()
                    .tableName("EngKing-ChatRoom")
                    .key(key)
                    .attributeUpdates(updates)
                    .build();

            dynamoDbClient.updateItem(request);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to update chat room score and feedback: " + e.getMessage());
            return false;
        }
    }

    // 채팅 메세지 내역 저장
    public boolean saveScoreAndFeedbackToDynamoDB(String chatRoomId, String messageTime, String messageId, String senderId, String audioFileUrl, String score, String feedback) {
        Map<String, AttributeValue> item = new HashMap<>();

        item.put("ChatRoomId", AttributeValue.builder().s(chatRoomId).build());
        item.put("MessageTime", AttributeValue.builder().s(messageTime).build());
        item.put("MessageId", AttributeValue.builder().s(messageId).build());
        item.put("SenderId", AttributeValue.builder().s(senderId).build());
        // Only add the AudioFileUrl if it is not null
        if (audioFileUrl != null) {
            item.put("AudioFileUrl", AttributeValue.builder().s(audioFileUrl).build());
        }
        item.put("Score", AttributeValue.builder().s(score).build());
        item.put("Feedback", AttributeValue.builder().s(feedback).build());


        PutItemRequest request = PutItemRequest.builder()
                .tableName("EngKing-ChatMessages")
                .item(item)
                .build();

        try {
            dynamoDbClient.putItem(request);
            // If no exception is thrown, saving is successful
            return true;
        } catch (Exception e) {
            // If an exception is thrown, saving failed
            System.err.println("Failed to save chat message to DynamoDB: " + e.getMessage());
            return false;
        }
    }

    // 특정 chatRoomId, senderId, messageId로 항목 삭제
    public boolean deleteChatMessageByChatRoomIdSenderIdAndMessageId(String senderId, String chatRoomId, String messageId) {
        // Step 1: Query to find all items with the specified ChatRoomId
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":ChatRoomId", AttributeValue.builder().s(chatRoomId).build());

        QueryRequest queryRequest = QueryRequest.builder()
                .tableName("EngKing-ChatMessages")
                .keyConditionExpression("ChatRoomId = :ChatRoomId")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        try {
            QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
            List<Map<String, AttributeValue>> items = queryResponse.items();

            if (items.isEmpty()) {
                System.err.println("No items found with the specified ChatRoomId.");
                return false; // 조회된 항목이 없을 경우
            }

            // Step 2: Find the item with the matching SenderId and MessageId
            for (Map<String, AttributeValue> item : items) {
                String currentSenderId = item.get("SenderId").s();
                String currentMessageId = item.get("MessageId").s();

                if (currentSenderId.equals(senderId) && currentMessageId.equals(messageId)) {
                    // Found the item to delete
                    String messageTime = item.get("MessageTime").s(); // Get MessageTime from the item

                    // Step 3: Delete the item using ChatRoomId and MessageTime
                    Map<String, AttributeValue> key = new HashMap<>();
                    key.put("ChatRoomId", AttributeValue.builder().s(chatRoomId).build());
                    key.put("MessageTime", AttributeValue.builder().s(messageTime).build());

                    DeleteItemRequest deleteRequest = DeleteItemRequest.builder()
                            .tableName("EngKing-ChatMessages")
                            .key(key)
                            .build();

                    try {
                        dynamoDbClient.deleteItem(deleteRequest);
                        // 삭제 성공
                        return true;
                    } catch (Exception e) {
                        System.err.println("Failed to delete chat message: " + e.getMessage());
                        return false;
                    }
                }
            }

            // If no matching item was found
            System.err.println("No matching item found with the specified SenderId and MessageId.");
            return false;

        } catch (Exception e) {
            System.err.println("Failed to query chat messages by ChatRoomId: " + e.getMessage());
            return false;
        }
    }



    // 추가적인 비즈니스 로직 구현...
}
