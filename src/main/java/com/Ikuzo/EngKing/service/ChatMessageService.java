package com.Ikuzo.EngKing.service;

import com.Ikuzo.EngKing.dto.ChatMessageResponseDto;
import com.Ikuzo.EngKing.dto.LangchainMessageRequestDto;
import com.Ikuzo.EngKing.dto.LangchainMessageResponseDto;
import com.Ikuzo.EngKing.entity.ChatMessages;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatMessageService {

    private final DynamoDbClient dynamoDbClient;
    private final RestTemplate restTemplate;

    public ChatMessageService(DynamoDbClient dynamoDbClient, RestTemplate restTemplate) {
        this.dynamoDbClient = dynamoDbClient;
        this.restTemplate = restTemplate;
    }

//    public ChatMessages addMessageToChatRoom(String chatRoomId, ChatMessages message) {
//        ChatRoom chatRoom = getChatRoomFromDynamoDB(chatRoomId, message.getSenderId());
//        if (chatRoom != null) {
//
//            // 외부 서버로 POST 요청 보내기
//            String url = "http://langchain-server-url/api/processMessage";
//            LangchainMessageRequestDto langchainMessageRequestDto = LangchainMessageRequestDto.from(chatRoomId, message.getMessageText());
//            String responseMessage = restTemplate.postForObject(url, langchainMessageRequestDto, String.class);
//
//            // 응답받은 메시지로 기존 메시지 내용 대체
//            message.setMessageText(responseMessage);
//
//            // DynamoDB에 새 메시지 저장
//            saveChatMessageToDynamoDB(message);
//
//            return message;
//        } else {
//            throw new RuntimeException("Chat room not found with id: " + chatRoomId);
//        }
//    }
//
//    private void saveChatMessageToDynamoDB(ChatMessages chatMessage) {
//        Map<String, AttributeValue> item = new HashMap<>();
//        item.put("ChatRoomId", AttributeValue.builder().s(chatMessage.getChatRoomId()).build());
//        item.put("MessageTime", AttributeValue.builder().s(ChatMessages.LocalDateTimeConverter.convert(chatMessage.getMessageTime())).build());
//        item.put("MessageId", AttributeValue.builder().s(chatMessage.getMessageId()).build());
//        item.put("SenderId", AttributeValue.builder().s(chatMessage.getSenderId()).build());
//        item.put("MessageText", AttributeValue.builder().s(chatMessage.getMessageText()).build());
//        item.put("AudioFileUrl", AttributeValue.builder().s(chatMessage.getAudioFileUrl()).build());
//
//        PutItemRequest request = PutItemRequest.builder()
//                .tableName("EngKing-ChatMessages")
//                .item(item)
//                .build();
//
//        dynamoDbClient.putItem(request);
//    }
//
//    private ChatRoom getChatRoomFromDynamoDB(String chatRoomId, String memberId) {
//        Map<String, AttributeValue> key = new HashMap<>();
//        key.put("ChatRoomId", AttributeValue.builder().s(chatRoomId).build());
//        key.put("MemberId", AttributeValue.builder().s(memberId).build());
//
//        GetItemRequest request = GetItemRequest.builder()
//                .tableName("EngKing-ChatRoom")
//                .key(key)
//                .build();
//
//        GetItemResponse response = dynamoDbClient.getItem(request);
//
//        if (response.hasItem()) {
//            Map<String, AttributeValue> item = response.item();
//            ChatRoom chatRoom = new ChatRoom();
//            chatRoom.setChatRoomId(item.get("ChatRoomId").s());
//            chatRoom.setMemberId(item.get("MemberId").s());
//            chatRoom.setDifficulty(item.get("Difficulty").s());
//            chatRoom.setTopic(item.get("Topic").s());
//            chatRoom.setCreatedTime(ChatRoom.LocalDateTimeConverter.unconvert(item.get("CreatedTime").s()));
//            return chatRoom;
//        } else {
//            return null;
//        }
//    }

    public List<ChatMessageResponseDto> selectChatMessagesByChatRoomId(String chatRoomId) {
        // DynamoDB에서 ChatRoomId로 ChatMessages 조회
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":ChatRoomId", AttributeValue.builder().s(chatRoomId).build());

        // Query 요청 생성
        QueryRequest queryRequest = QueryRequest.builder()
                .tableName("EngKing-ChatMessages")
                .keyConditionExpression("ChatRoomId = :ChatRoomId")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        try {
            QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
            List<Map<String, AttributeValue>> items = queryResponse.items();

            if (items.isEmpty()) {
                return new ArrayList<>();  // 조회된 항목이 없을 경우 빈 리스트 반환
            }

            // 항목을 ChatMessageResponseDto로 변환
            List<ChatMessageResponseDto> responseDtos = new ArrayList<>();
            for (Map<String, AttributeValue> item : items) {
                ChatMessageResponseDto responseDto = new ChatMessageResponseDto();

                // 각 속성에 대해 null 체크 수행
                responseDto.setChatRoomId(item.get("ChatRoomId") != null ? item.get("ChatRoomId").s() : null);
                responseDto.setMessageTime(item.get("MessageTime") != null ?
                        ChatMessages.LocalDateTimeConverter.unconvert(item.get("MessageTime").s()) : null);
                responseDto.setMessageId(item.get("MessageId") != null ? item.get("MessageId").s() : null);
                responseDto.setSenderId(item.get("SenderId") != null ? item.get("SenderId").s() : null);
                responseDto.setMessageText(item.get("MessageText") != null ? item.get("MessageText").s() : null);
                responseDto.setAudioFileUrl(item.get("AudioFileUrl") != null ? item.get("AudioFileUrl").s() : null);
                responseDto.setScore(item.get("Score") != null ? item.get("Score").s() : null);
                responseDto.setFeedback(item.get("Feedback") != null ? item.get("Feedback").s() : null);

                responseDtos.add(responseDto);
            }

            return responseDtos;

        } catch (Exception e) {
            System.err.println("Failed to query chat messages by ChatRoomId: " + e.getMessage());
            return new ArrayList<>();
        }
    }

}
