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
public class QuestionController {

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

            // 메시지 ID와 시간을 생성하여 DynamoDB에 저장
            String messageId = "1";  // 첫 메시지이므로 ID는 1
            String messageTime = LocalDateTime.now().withNano(0).toString();
            String audioUrl = null;

            boolean saveSuccess = questionService.saveChatMessageToDynamoDB(
                    questionResponseDto.getChatRoomId(),
                    messageTime,
                    messageId,
                    memberId,  // 발신자 == 사용자
                    langChainMessage,
                    audioUrl // 오디오 파일 URL이 없는 경우 null 처리
            );

            if (!saveSuccess) {
                log.error("Failed to save first question to DynamoDB.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            questionResponseDto.setFirstQuestion(langChainMessage);
            questionResponseDto.setMessageId(messageId);
            return ResponseEntity.status(HttpStatus.OK).body(questionResponseDto);
        }
        // 채팅방 생성 실패시
        else {
            QuestionResponseDto badQuestionResponseDto = new QuestionResponseDto();
            badQuestionResponseDto.setMessageId("1");
            badQuestionResponseDto.setTopic(topic);
            badQuestionResponseDto.setDifficulty(difficulty);
            badQuestionResponseDto.setSuccess(false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(badQuestionResponseDto);
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

        String AnswerMessageTime = LocalDateTime.now().withNano(0).toString();
        String AnswerAudioUrl = null;

        boolean answerSaveSuccess = questionService.saveChatMessageToDynamoDB(
                chatRoomId,
                AnswerMessageTime,
                messageId,
                memberId,
                messageText,
                AnswerAudioUrl // 오디오 파일 URL이 없는 경우 null 처리
        );

        if (!answerSaveSuccess) {
            log.error("Failed to save next question to DynamoDB.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        String nextQuestion = questionService.createQuestion(memberId, chatRoomId, messageText, difficulty, topic, false);

        int number = Integer.parseInt(messageId);
        number += 1;
        String nextMessageId = Integer.toString(number);
        String QuestionMessageTime = LocalDateTime.now().withNano(0).toString();
        String questionAudioUrl = null;


        // DynamoDB에 다음 질문 저장
        boolean questionSaveSuccess = questionService.saveChatMessageToDynamoDB(
                chatRoomId,
                QuestionMessageTime,
                nextMessageId,
                memberId,
                nextQuestion,
                questionAudioUrl // 오디오 파일 URL이 없는 경우 null 처리
        );

        if (!questionSaveSuccess) {
            log.error("Failed to save next question to DynamoDB.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        QuestionResponseDto questionResponseDto = new QuestionResponseDto();
        questionResponseDto.setChatRoomId(chatRoomId);
        questionResponseDto.setMemberId(memberId);
        questionResponseDto.setMessageId(nextMessageId);
        questionResponseDto.setNextQuestion(nextQuestion);
        questionResponseDto.setCreatedTime(LocalDateTime.now().withNano(0));
        questionResponseDto.setSuccess(true);
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

            if (questionResponseDto != null && questionResponseDto.getScore() != null && questionResponseDto.getFeedback() != null) {
                boolean updateSuccess = questionService.updateChatRoomScoreAndFeedback(chatRoomId, memberId, questionResponseDto.getScore(), questionResponseDto.getFeedback());

                if (!updateSuccess) {
                    log.error("Failed to update score and feedback in DynamoDB.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }

                int number = Integer.parseInt(messageId);
                number += 1;
                String nextMessageId = Integer.toString(number);
                questionResponseDto.setChatRoomId(chatRoomId);
                questionResponseDto.setMemberId(memberId);
                questionResponseDto.setMessageId(nextMessageId);
                questionResponseDto.setMessageTime(LocalDateTime.now().withNano(0));
                questionResponseDto.setSuccess(true);

                return ResponseEntity.status(HttpStatus.OK).body(questionResponseDto);
            } else {
                log.error("Score or feedback is missing.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            QuestionResponseDto questionResponseDto = new QuestionResponseDto();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(questionResponseDto);
        }
    }


    // 컨트롤러 추가

}
