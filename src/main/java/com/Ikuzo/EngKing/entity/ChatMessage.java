package com.Ikuzo.EngKing.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ChatMessage {
    private ObjectId messageId;  // 메시지 ID (자동 생성)

    private String senderId;  // 발신자 ID (회원 ID 등)
    private String content;  // 메시지 내용
    private LocalDateTime sentTime;  // 메시지 전송 시간

    // 기본 생성자
    public ChatMessage(String senderId, String content, LocalDateTime sentTime) {
        this.messageId = new ObjectId();  // 자동으로 ObjectId 생성
        this.senderId = senderId;
        this.content = content;
        this.sentTime = sentTime;
    }

    // 모든 필드를 포함하는 생성자 (사용하지 않을 것을 권장)
    public ChatMessage(ObjectId messageId, String senderId, String content, LocalDateTime sentTime) {
        this.messageId = messageId != null ? messageId : new ObjectId();  // null일 경우에만 새 ObjectId 생성
        this.senderId = senderId;
        this.content = content;
        this.sentTime = sentTime;
    }
}
