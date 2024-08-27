package com.Ikuzo.EngKing.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "chat_room")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ChatRoom {
    @Id
    private String id;  // MongoDB의 기본 키

    private String memberId;  // MySQL의 회원 ID를 참조
    private String topic;  // 대화 주제
    private String difficulty;  // 대화 난이도
    private LocalDateTime createdTime;  // 대화방 생성 시간
    private List<ChatMessage> messages;  // 대화방의 메시지 목록

    public ChatRoom(String memberId, String topic, String difficulty, LocalDateTime createdTime, List<ChatMessage> messages) {
        this.memberId = memberId;
        this.topic = topic;
        this.difficulty = difficulty;
        this.createdTime = createdTime;
        this.messages = messages;
    }
}
