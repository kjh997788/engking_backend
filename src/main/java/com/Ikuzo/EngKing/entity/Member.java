//package com.Ikuzo.EngKing.entity;
//
//import com.Ikuzo.EngKing.constant.Authority;
//import com.Ikuzo.EngKing.constant.Existence;
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//
//
//@Entity // 디비로 생성
//@Table(name = "member") // 테이블 이름 정의
//@Getter
//@Setter
//@ToString
//@NoArgsConstructor // 디폴트 생성자 생성
//public class Member {
//    @Id
//    @Column(name = "member_id")
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long memberId;
//    @Column(unique = true)
//    private String email;
//    private String password;
//    private String name;
//    @Column(unique = true)
//    private String phone;
//    private LocalDate birthday;
//    private LocalDateTime signUpTime;
//    private String intro;
//    @Enumerated(EnumType.STRING)
//    private Authority authority;
//    @Enumerated(EnumType.STRING)
//    private Existence existence;
//
//    @Builder
//    public Member(String email, String password, String name, String phone, LocalDate birthday, LocalDateTime signUpTime, Authority authority, Existence existence) {
//        this.email = email;
//        this.password = password;
//        this.name = name;
//        this.phone = phone;
//        this.birthday = birthday;
//        this.signUpTime =signUpTime;
//        this.authority = authority;
//    }
//
//    // 타 테이블 맵핑
//
//
//}
