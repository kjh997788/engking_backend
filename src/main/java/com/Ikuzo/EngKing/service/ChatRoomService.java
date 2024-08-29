package com.Ikuzo.EngKing.service;

import com.Ikuzo.EngKing.dto.ChatRoomResponseDto;
import com.Ikuzo.EngKing.entity.ChatRoom;
import com.Ikuzo.EngKing.entity.ChatMessages;
import com.Ikuzo.EngKing.dto.LangchainMessageRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatRoomService {

    private final DynamoDbClient dynamoDbClient;
    private final RestTemplate restTemplate;

    @Autowired
    public ChatRoomService(DynamoDbClient dynamoDbClient, RestTemplate restTemplate) {
        this.dynamoDbClient = dynamoDbClient;
        this.restTemplate = restTemplate;
    }

    public ChatRoomResponseDto createChatRoom(String memberId, String topic, String difficulty) {
        LocalDateTime rightNow = LocalDateTime.now();
        String chatRoomId = memberId + "_" + rightNow.toString();

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(chatRoomId); // DynamoDB의 HashKey로 사용
        chatRoom.setMemberId(memberId); // DynamoDB의 RangeKey로 사용
        chatRoom.setTopic(topic);
        chatRoom.setDifficulty(difficulty);
        chatRoom.setCreatedTime(rightNow);
        // DynamoDB에 ChatRoom 저장
        boolean dynamoDBResponse = saveChatRoomToDynamoDB(chatRoom);
        // 컨트롤러에 반환 객체
        ChatRoomResponseDto chatRoomResponseDto = new ChatRoomResponseDto();
        chatRoomResponseDto.setChatRoomId(chatRoomId);
        chatRoomResponseDto.setSuccess(dynamoDBResponse);
        chatRoomResponseDto.setCreatedTime(rightNow);

        return chatRoomResponseDto;
    }
    
    // 채팅방 생성됨을 DynamoDB에 저장
    private boolean saveChatRoomToDynamoDB(ChatRoom chatRoom) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ChatRoomId", AttributeValue.builder().s(chatRoom.getChatRoomId()).build());
        item.put("MemberId", AttributeValue.builder().s(chatRoom.getMemberId()).build());
        item.put("Difficulty", AttributeValue.builder().s(chatRoom.getDifficulty()).build());
        item.put("Topic", AttributeValue.builder().s(chatRoom.getTopic()).build());
        item.put("CreatedTime", AttributeValue.builder().s(ChatRoom.LocalDateTimeConverter.convert(chatRoom.getCreatedTime())).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName("EngKing-ChatRoom")
                .item(item)
                .build();

        try {
            PutItemResponse response = dynamoDbClient.putItem(request);
            // 예외가 발생하지 않으면 저장이 성공한 것입니다.
            return true;
        } catch (Exception e) {
            // 예외가 발생하면 저장에 실패한 것입니다.
            System.err.println("Failed to save chat room to DynamoDB: " + e.getMessage());
            return false;
        }
    }


    public ChatMessages addMessageToChatRoom(String chatRoomId, ChatMessages message) {
        ChatRoom chatRoom = getChatRoomFromDynamoDB(chatRoomId, message.getSenderId());
        if (chatRoom != null) {

            // 외부 서버로 POST 요청 보내기
            String url = "http://langchain-server-url/api/processMessage";
            LangchainMessageRequestDto langchainMessageRequestDto = LangchainMessageRequestDto.from(chatRoomId, message.getMessageText());
            String responseMessage = restTemplate.postForObject(url, langchainMessageRequestDto, String.class);

            // 응답받은 메시지로 기존 메시지 내용 대체
            message.setMessageText(responseMessage);

            // DynamoDB에 새 메시지 저장
            saveChatMessageToDynamoDB(message);

            return message;
        } else {
            throw new RuntimeException("Chat room not found with id: " + chatRoomId);
        }
    }

    private void saveChatMessageToDynamoDB(ChatMessages chatMessage) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ChatRoomId", AttributeValue.builder().s(chatMessage.getChatRoomId()).build());
        item.put("MessageTime", AttributeValue.builder().s(ChatMessages.LocalDateTimeConverter.convert(chatMessage.getMessageTime())).build());
        item.put("MessageId", AttributeValue.builder().s(chatMessage.getMessageId()).build());
        item.put("SenderId", AttributeValue.builder().s(chatMessage.getSenderId()).build());
        item.put("MessageText", AttributeValue.builder().s(chatMessage.getMessageText()).build());
        item.put("AudioFileUrl", AttributeValue.builder().s(chatMessage.getAudioFileUrl()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName("EngKing-ChatMessages")
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }

    private ChatRoom getChatRoomFromDynamoDB(String chatRoomId, String memberId) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("ChatRoomId", AttributeValue.builder().s(chatRoomId).build());
        key.put("MemberId", AttributeValue.builder().s(memberId).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName("EngKing-ChatRoom")
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        if (response.hasItem()) {
            Map<String, AttributeValue> item = response.item();
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setChatRoomId(item.get("ChatRoomId").s());
            chatRoom.setMemberId(item.get("MemberId").s());
            chatRoom.setDifficulty(item.get("Difficulty").s());
            chatRoom.setTopic(item.get("Topic").s());
            chatRoom.setCreatedTime(ChatRoom.LocalDateTimeConverter.unconvert(item.get("CreatedTime").s()));
            return chatRoom;
        } else {
            return null;
        }
    }

    // 추가적인 비즈니스 로직 구현...
}
