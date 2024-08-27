package com.Ikuzo.EngKing.service;

import com.Ikuzo.EngKing.entity.ChatRoom;
import com.Ikuzo.EngKing.entity.ChatMessage;
import com.Ikuzo.EngKing.repository.ChatRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    public ChatRoom createChatRoom(String memberId, String topic, String difficulty) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setMemberId(memberId);
        chatRoom.setTopic(topic);
        chatRoom.setDifficulty(difficulty);
        chatRoom.setCreatedTime(LocalDateTime.now());
        chatRoom.setMessages(List.of());  // 초기에는 빈 메시지 리스트

        return chatRoomRepository.save(chatRoom);
    }

    public ChatMessage addMessageToChatRoom(String chatRoomId, ChatMessage message) {
        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findById(chatRoomId);

        if (optionalChatRoom.isPresent()) {
            ChatRoom chatRoom = optionalChatRoom.get();
            chatRoom.getMessages().add(message);
            chatRoomRepository.save(chatRoom);
            return message;
        } else {
            throw new RuntimeException("Chat room not found with id: " + chatRoomId);
        }
    }

    public List<ChatRoom> getChatRoomsByMemberId(String memberId) {
        return chatRoomRepository.findByMemberId(memberId);
    }

    // 추가적인 비즈니스 로직 구현...
}
