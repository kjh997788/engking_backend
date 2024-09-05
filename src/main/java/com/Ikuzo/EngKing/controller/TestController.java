package com.Ikuzo.EngKing.controller;

import com.Ikuzo.EngKing.dto.QuestionRequestDto;
import com.Ikuzo.EngKing.dto.QuestionResponseDto;
import com.Ikuzo.EngKing.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/audio")
public class TestController {
    private final S3Service s3Service;

    // 음성 답변 파일 업로드용 pre-signed url 반환
    @PostMapping("/uploadurl")
    public ResponseEntity<QuestionResponseDto> generateAudioPreSignedUrl(@RequestBody QuestionRequestDto questionRequestDto) {
        String memberId = questionRequestDto.getMemberId();
        String chatRoomId = questionRequestDto.getChatRoomId();
        String messageId = questionRequestDto.getMessageId();

        QuestionResponseDto questionResponseDto = new QuestionResponseDto();
        String preSignedUrl = s3Service.generateUploadPreSignedUrl(memberId, chatRoomId, messageId);
        questionResponseDto.setAudioFileUrl(preSignedUrl);
        questionResponseDto.setSuccess(true);

        return ResponseEntity.status(HttpStatus.OK).body(questionResponseDto);
    }





}
