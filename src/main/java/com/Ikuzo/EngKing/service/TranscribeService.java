package com.Ikuzo.EngKing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class TranscribeService {

    private final TranscribeClient transcribeClient;

    public Boolean startTranscriptionJob(String memberId, String chatRoomId, String messageId) {
        // 형식에 맞게 LocalDateTime을 String으로 변환합니다.
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String jobName = String.format("audio-%s-%s-%s", memberId, messageId, timestamp);
        String mediaUri = String.format("s3://engking-voice-bucket/audio/%s/%s/%s.mp3", memberId, chatRoomId, messageId);
//        String outputPath = String.format("audio/%s/%s/%s", memberId, chatRoomId, messageId); - 불가능

        // Transcribe 작업을 시작하기 위한 요청을 생성
        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .languageCode(LanguageCode.EN_US) // 필요한 언어 코드로 설정
                .media(Media.builder().mediaFileUri(mediaUri).build())
                .mediaFormat(MediaFormat.MP3)  // 미디어 파일 형식 설정 (MP3, MP4, WAV, FLAC 등)
                .outputBucketName("engking-bucket-dev")  // 결과를 저장할 S3 버킷 이름
//                .outputKey(outputPath)  // 버킷 내부의 폴더 경로 지정 - 불가능
                .build();

        // Transcribe 작업 시작
        transcribeClient.startTranscriptionJob(request);

        return true;
    }

    public boolean waitForTranscriptionToComplete(String memberId, String chatRoomId, String messageId) {
        String jobName = String.format("audio-%s-%s-%s", memberId, chatRoomId, messageId);

        try {
            while (true) {
                GetTranscriptionJobRequest getRequest = GetTranscriptionJobRequest.builder()
                        .transcriptionJobName(jobName)
                        .build();

                GetTranscriptionJobResponse getResponse = transcribeClient.getTranscriptionJob(getRequest);
                TranscriptionJobStatus status = getResponse.transcriptionJob().transcriptionJobStatus();

                if (status == TranscriptionJobStatus.COMPLETED) {
                    return true;
                } else if (status == TranscriptionJobStatus.FAILED) {
                    System.out.print("Transcription job failed.");
                    return false;
                }

                // 잠시 대기 후 다시 확인 (예: 5초)
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            System.out.print("Error while waiting for transcription to complete");
            return false;
        }
    }

}
