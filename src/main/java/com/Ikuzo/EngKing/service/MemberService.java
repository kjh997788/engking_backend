//package com.Ikuzo.EngKing.service;
//
//import com.Ikuzo.EngKing.constant.Authority;
//import com.Ikuzo.EngKing.constant.Existence;
//import com.Ikuzo.EngKing.dto.MemberRequestDto;
//import com.Ikuzo.EngKing.entity.Member;
//import com.Ikuzo.EngKing.repository.MemberRepository;
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Service
//@Transactional
//@Slf4j
//@RequiredArgsConstructor
//public class MemberService {
//    private final AuthenticationManagerBuilder managerBuilder;
//    private final MemberRepository memberRepository;
//    private final HttpSession session;
//    private final PasswordEncoder passwordEncoder;
//
//    public Member registerMember(MemberRequestDto memberRequestDto) {
//        // 패스워드 암호화
//        String encodedPassword = passwordEncoder.encode(memberRequestDto.getPassword());
//
//        // 회원 엔티티 생성
//        Member member = Member.builder()
//                .email(memberRequestDto.getEmail())
//                .password(encodedPassword)
//                .name(memberRequestDto.getName())
//                .phone(memberRequestDto.getPhone())
//                .birthday(memberRequestDto.getBirthday())
//                .signUpTime(LocalDateTime.now())
//                .authority(Authority.ROLE_MEMBER) // 기본 권한 설정
//                .existence(Existence.YES) // 기본 존재 상태 설정
//                .build();
//
//        // 회원 저장
//        return memberRepository.save(member);
//    }
//
//    public Member loginMember(String email, String password) throws Exception {
//        // 이메일로 회원 조회
//        Optional<Member> optionalMember = memberRepository.findByEmail(email);
//        if (optionalMember.isPresent()) {
//            Member member = optionalMember.get();
//            // 비밀번호 검증
//            if (passwordEncoder.matches(password, member.getPassword())) {
//                // 세션에 회원 정보 저장
//                session.setAttribute("member", member);
//                return member;
//            } else {
//                throw new Exception("비밀번호가 일치하지 않습니다.");
//            }
//        } else {
//            throw new Exception("회원 정보를 찾을 수 없습니다.");
//        }
//    }
//
//    public Member memberList(String email) {
//        Optional<Member> optionalMember =memberRepository.findByEmail(email);
//        Member member = optionalMember.get();
//        // 모든 회원 정보 조회
//        return member;
//    }
//}
