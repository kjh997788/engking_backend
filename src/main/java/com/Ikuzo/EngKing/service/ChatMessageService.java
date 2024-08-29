package com.Ikuzo.EngKing.service;

import com.Ikuzo.EngKing.dto.LangchainMessageRequestDto;
import com.Ikuzo.EngKing.dto.LangchainMessageResponseDto;
import com.Ikuzo.EngKing.entity.ChatMessages;
import com.Ikuzo.EngKing.entity.ChatRoom;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatMessageService {

    private final DynamoDbClient dynamoDbClient;
    private final RestTemplate restTemplate;

    public ChatMessageService(DynamoDbClient dynamoDbClient, RestTemplate restTemplate) {
        this.dynamoDbClient = dynamoDbClient;
        this.restTemplate = restTemplate;
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
}
