package com.Ikuzo.EngKing.controller;

import com.Ikuzo.EngKing.dto.QuestionRequestDto;
import com.Ikuzo.EngKing.dto.QuestionResponseDto;
import com.Ikuzo.EngKing.service.*;
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
    private final QuizService quizService;
    private final TranscribeService transcribeService;
    private final PollyService pollyService;
    private final S3Service s3Service;

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
//            String audioUrl = "s3://engking-voice-bucket/audio/" + memberId + "/" + questionResponseDto.getChatRoomId() + "/" + messageId + ".mp3";

            boolean saveSuccess = questionService.saveChatMessageToDynamoDB(
                    questionResponseDto.getChatRoomId(),
                    messageTime,
                    messageId,
                    "AI",
                    langChainMessage,
                    audioUrl // 오디오 파일 URL이 없는 경우 null 처리
            );

            if (!saveSuccess) {
                log.error("Failed to save first question to DynamoDB.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            //테스트 코드
            Boolean pollySuccess = pollyService.synthesizeTextAndUploadToS3(memberId, questionResponseDto.getChatRoomId(), messageId, langChainMessage);

            if (pollySuccess) {
                // Polly 작업이 완료된 후에 S3 URL 생성
                String audioFileUrl = s3Service.generatePreSignedUrl(memberId, questionResponseDto.getChatRoomId(), messageId);
                questionResponseDto.setAudioFileUrl(audioFileUrl);
            } else {
                log.error("Failed to synthesize text and upload to S3.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
            // polly 종료
            
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
//        String messageText = questionRequestDto.getMessageText();
// 테스트 코드 - transcribe 시작
        Boolean transcribeStarted = transcribeService.startTranscriptionJob(memberId, chatRoomId, messageId);

        if (!transcribeStarted) {
            log.error("Failed to start transcription job.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // Transcribe 작업이 완료될 때까지 대기
        boolean isTranscribeCompleted = transcribeService.waitForTranscriptionToComplete(memberId, chatRoomId, messageId);
        if (!isTranscribeCompleted) {
            log.error("Transcription job did not complete successfully.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        // Transcribe 작업이 완료된 후 S3에서 텍스트 가져오기
        String messageText = s3Service.getTranscriptFromS3(memberId, chatRoomId, messageId);

        // 정상 코드
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
                "AI",
                nextQuestion,
                questionAudioUrl // 오디오 파일 URL이 없는 경우 null 처리
        );

        if (!questionSaveSuccess) {
            log.error("Failed to save next question to DynamoDB.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        QuestionResponseDto questionResponseDto = new QuestionResponseDto();
        
        // 테스트 코드
        Boolean pollySuccess = pollyService.synthesizeTextAndUploadToS3(memberId, chatRoomId, nextMessageId, nextQuestion);
        if (pollySuccess) {
            // Polly 작업이 완료된 후에 S3 URL 생성
            String audioFileUrl = s3Service.generatePreSignedUrl(memberId, chatRoomId, messageId);
            questionResponseDto.setAudioFileUrl(audioFileUrl);
        } else {
            log.error("Failed to synthesize text and upload to S3.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        // polly 종료
        
        questionResponseDto.setChatRoomId(chatRoomId);
        questionResponseDto.setMemberId(memberId);
        questionResponseDto.setMessageId(nextMessageId);
        questionResponseDto.setNextQuestion(nextQuestion);
        // 나중에 수정 필요
        questionResponseDto.setAudioFileUrl(s3Service.generatePreSignedUrl(memberId, chatRoomId, nextMessageId));
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
        String AnswerAudioUrl = null;

        if (endRequest) {
            int number = Integer.parseInt(messageId);
            number -= 1;
            String nextMessageId = Integer.toString(number);

            boolean deleteSuccess = quizService.deleteChatMessageByChatRoomIdSenderIdAndMessageId("AI", chatRoomId, nextMessageId);
            QuestionResponseDto questionResponseDto = questionService.endQuestion(memberId, chatRoomId);
            String messageTime = LocalDateTime.now().withNano(0).toString();

            if (questionResponseDto != null && questionResponseDto.getScore() != null && questionResponseDto.getFeedback() != null) {
                boolean updateSuccess = questionService.updateChatRoomScoreAndFeedback(chatRoomId, memberId, questionResponseDto.getScore(), questionResponseDto.getFeedback());
                boolean updateMessageSuccess = quizService.saveScoreAndFeedbackToDynamoDB(chatRoomId, messageTime, nextMessageId, "AI", AnswerAudioUrl, questionResponseDto.getScore(), questionResponseDto.getFeedback());

                // 테스트 코드 추가 삭제 필요
                Boolean polly = pollyService.synthesizeTextAndUploadToS3(memberId, chatRoomId, nextMessageId, questionResponseDto.getFeedback());
                
                if (!updateSuccess || !updateMessageSuccess) {
                    log.error("Failed to update score and feedback in DynamoDB.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }

                questionResponseDto.setChatRoomId(chatRoomId);
                questionResponseDto.setMemberId(memberId);
                questionResponseDto.setMessageId(nextMessageId);
                questionResponseDto.setMessageTime(LocalDateTime.now().withNano(0));
                questionResponseDto.setSuccess(true);

                // 테스트 코드 추가 삭제 필요
                questionResponseDto.setAudioFileUrl(s3Service.generatePreSignedUrl(memberId, chatRoomId, nextMessageId));

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
