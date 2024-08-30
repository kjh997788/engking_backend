package com.Ikuzo.EngKing.controller;

import com.Ikuzo.EngKing.dto.QuestionRequestDto;
import com.Ikuzo.EngKing.dto.QuestionResponseDto;
import com.Ikuzo.EngKing.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class questionController {

    private final QuestionService questionService;

    @PostMapping("/firstquestion")
    public ResponseEntity<QuestionResponseDto> createFirstQuestion(@RequestBody QuestionRequestDto questionRequestDto) {
        String memberId = questionRequestDto.getMemberId();
        String topic = questionRequestDto.getTopic();
        String difficulty = questionRequestDto.getDifficulty();

        QuestionResponseDto questionResponseDto = questionService.createChatRoom(memberId, topic, difficulty);

        // 채팅방 생성 성공시, 첫 질문 생성 시도
        if (questionResponseDto.isSuccess()) {
            String langChainMessage = questionService.createQuestion(questionResponseDto.getMemberId(), questionResponseDto.getChatRoomId(), "Can you ask me a question?", difficulty, topic, true);
            questionResponseDto.setMessageText(langChainMessage);
            questionResponseDto.setMessageId("1");
            return ResponseEntity.status(HttpStatus.OK).body(questionResponseDto);
        }
        // 채팅방 생성 실패시
        else {
            QuestionResponseDto badQuestionResponseDto = new QuestionResponseDto();
            badQuestionResponseDto.setMessageId("1");
            badQuestionResponseDto.setTopic(topic);
            badQuestionResponseDto.setDifficulty(difficulty);
            badQuestionResponseDto.setSuccess(false);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(badQuestionResponseDto);
        }
    }

    @PostMapping("/nextquestion")
    public ResponseEntity<QuestionResponseDto> createNextQuestion(@RequestBody QuestionRequestDto questionRequestDto) {
        String memberId = questionRequestDto.getMemberId();
        String chatRoomId = questionRequestDto.getChatRoomId();
        String messageId = questionRequestDto.getMessageId();
        String messageText = questionRequestDto.getMessageText();
        String topic = questionRequestDto.getTopic();
        String difficulty = questionRequestDto.getDifficulty();

        String nextQuestion = questionService.createQuestion(memberId, chatRoomId, messageText, difficulty, topic, false);

        int number = Integer.parseInt(messageId);
        number += 1;
        String nextMessageId = Integer.toString(number);

        QuestionResponseDto questionResponseDto = new QuestionResponseDto();
        questionResponseDto.setChatRoomId(chatRoomId);
        questionResponseDto.setMemberId(memberId);
        questionResponseDto.setMessageId(nextMessageId);
        questionResponseDto.setNextQuestion(nextQuestion);
        questionResponseDto.setCreatedTime(LocalDateTime.now().withNano(0));
        return ResponseEntity.status(HttpStatus.OK).body(questionResponseDto);
    }

    @PostMapping("/endquestion")
    public ResponseEntity<QuestionResponseDto> endQuestion(@RequestBody QuestionRequestDto questionRequestDto) {
        String memberId = questionRequestDto.getMemberId();
        String chatRoomId = questionRequestDto.getChatRoomId();
        String messageId = questionRequestDto.getMessageId();
        Boolean endRequest = questionRequestDto.isEndRequest();

        if (endRequest) {
            QuestionResponseDto questionResponseDto = questionService.endQuestion(memberId, chatRoomId);

            int number = Integer.parseInt(messageId);
            number += 1;
            String nextMessageId = Integer.toString(number);
            questionResponseDto.setChatRoomId(chatRoomId);
            questionResponseDto.setMemberId(memberId);
            questionResponseDto.setMessageId(nextMessageId);
            questionResponseDto.setMessageTime(LocalDateTime.now().withNano(0));
            questionResponseDto.setSuccess(true);

            return ResponseEntity.status(HttpStatus.OK).body(questionResponseDto);
        }
        else {
            QuestionResponseDto questionResponseDto = new QuestionResponseDto();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(questionResponseDto);
        }
    }


    // quiz 해야지
    @PostMapping("/quizcreate")
    public ResponseEntity<QuestionResponseDto> createQuiz(@RequestBody QuestionRequestDto quizRequestDto) {


        QuestionResponseDto quizResponseDto = new QuestionResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(quizResponseDto);
    }

}
