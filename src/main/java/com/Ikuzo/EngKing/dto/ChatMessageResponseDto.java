package com.Ikuzo.EngKing.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON에서 제외
public class ChatMessageResponseDto {

    private String chatRoomId;    // DynamoDB의 파티션 키
    private LocalDateTime messageTime;  // DynamoDB의 정렬 키
    private String messageId;
    private String senderId;
    private String messageText;
    private String audioFileUrl;
    private String score;
    private String feedback;

}
