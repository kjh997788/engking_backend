package com.Ikuzo.EngKing.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LangchainMessageResponseDto {
    private String chatRoomId;
    private String chatMessageId;
    private String score;
    private String feedback;
    private String nextQuestion;

    public static LangchainMessageResponseDto from(String chatRoomId, String chatMessageId, String score, String feedback, String  nextQuestion) {
        return LangchainMessageResponseDto.builder()
                .chatRoomId(chatRoomId)
                .chatMessageId(chatMessageId)
                .score(score)
                .feedback(feedback)
                .nextQuestion(nextQuestion)
                .build();
    }
}
