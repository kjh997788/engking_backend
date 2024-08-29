package com.Ikuzo.EngKing.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class ChatRoom {

    private String chatRoomId;  // DynamoDB의 파티션 키
    private String memberId;
    private String difficulty;
    private String topic;
    private LocalDateTime createdTime;

    // LocalDateTime과 String 간 변환을 위한 헬퍼 메서드
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
