package com.Ikuzo.EngKing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity // 디비로 생성
@Table(name = "conversation") // 테이블 이름 정의
@Getter
@Setter
@ToString
@NoArgsConstructor // 디폴트 생성자 생성
public class Conversation {

    @Id
    @Column(name = "Conversation_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long conversationId;
}
