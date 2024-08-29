package com.Ikuzo.EngKing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomResponseDto {

    private String chatRoomId;  // DynamoDB의 파티션 키
    private String memberId;    // DynamoDB의 정렬 키
    private String difficulty;
    private String topic;
    private LocalDateTime createdTime;

    private boolean success; // db 저장 됬는지

}
