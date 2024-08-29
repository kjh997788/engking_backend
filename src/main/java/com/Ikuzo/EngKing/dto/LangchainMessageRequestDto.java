package com.Ikuzo.EngKing.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LangchainMessageRequestDto {
    private String chatRoomId;
    private String message;

    // 정적 팩토리 메서드 추가
    public static LangchainMessageRequestDto from(String chatRoomId, String message) {
        return LangchainMessageRequestDto.builder()
                .chatRoomId(chatRoomId)
                .message(message)
                .build();
    }
}
