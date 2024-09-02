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
public class QuestionRequestDto {

    private String chatRoomId;  // DynamoDB의 파티션 키
    private String memberId;    // DynamoDB의 정렬 키
    private String difficulty;
    private String topic;
    private String quiz_type;
    private LocalDateTime createdTime;

    private LocalDateTime messageTime;  // DynamoDB의 정렬 키
    private String messageId;
    private String senderId;
    private String messageText;
    private String audioFileUrl;

    private boolean success; // 디비 저장 관련
    private boolean endRequest; // 대화 종료 관련

}
