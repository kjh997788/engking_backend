package com.Ikuzo.EngKing.service;

import com.Ikuzo.EngKing.dto.ChatRoomResponseDto;
import com.Ikuzo.EngKing.entity.ChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.LocalDateTime;
import java.util.HashMap;
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
        LocalDateTime rightNow = LocalDateTime.now().withNano(0);
        String chatRoomId = memberId + "_" + rightNow.toString();

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setChatRoomId(chatRoomId); // DynamoDB의 HashKey로 사용
        chatRoom.setMemberId(memberId); // DynamoDB의 RangeKey로 사용
        chatRoom.setTopic(topic);
        chatRoom.setDifficulty(difficulty);
        chatRoom.setCreatedTime(rightNow);
        // DynamoDB에 ChatRoom 저장
        boolean dynamoDBResponse = saveChatRoomToDynamoDB(chatRoom);
        // 컨트롤러에 반환하는 객체
        ChatRoomResponseDto chatRoomResponseDto = new ChatRoomResponseDto();
        chatRoomResponseDto.setChatRoomId(chatRoomId);
        chatRoomResponseDto.setMemberId(memberId);
        chatRoomResponseDto.setCreatedTime(rightNow);
        chatRoomResponseDto.setSuccess(dynamoDBResponse);

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
            // 예외가 발생하지 않으면 저장이 성공
            return true;
        } catch (Exception e) {
            // 예외가 발생하면 저장에 실패
            System.err.println("Failed to save chat room to DynamoDB: " + e.getMessage());
            return false;
        }
    }


    // 추가적인 비즈니스 로직 구현...
}
