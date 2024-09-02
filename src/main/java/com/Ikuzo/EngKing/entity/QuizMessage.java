package com.Ikuzo.EngKing.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class QuizMessage {
    private String quizRoomId;    // DynamoDB의 파티션 키
    private LocalDateTime messageTime;  // DynamoDB의 정렬 키
    private String messageId;
    private String senderId;
    private String messageText;
    private String audioFileUrl;

    public static class LocalDateTimeConverter {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        public static String convert(LocalDateTime time) {
            return time.format(FORMATTER);
        }

        public static LocalDateTime unconvert(String stringValue) {
            return LocalDateTime.parse(stringValue, FORMATTER);
        }
    }

}
