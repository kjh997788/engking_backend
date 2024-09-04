package com.Ikuzo.EngKing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PollyService {

    private final PollyClient pollyClient;
    private final S3Client s3Client;  // S3 클라이언트 추가

    public Boolean synthesizeTextAndUploadToS3(String memberId, String chatRoomId, String messageId, String messageText) {
        String bucketName = "engking-voice-bucket";
        String objectKey = String.format("audio/%s/%s/%s.mp3", memberId, chatRoomId, messageId);

        // AWS Polly를 사용하여 텍스트를 음성으로 변환
        SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
                .text(messageText)
                .voiceId(VoiceId.JOANNA)  // 사용할 음성 설정 (다른 옵션도 사용 가능)
                .outputFormat(OutputFormat.MP3)  // 출력 형식 설정 (MP3, OGG, PCM 등)
                .build();

        try (ResponseInputStream<SynthesizeSpeechResponse> synthRes = pollyClient.synthesizeSpeech(synthReq)) {
            // 변환된 음성을 S3에 업로드
            uploadToS3(bucketName, objectKey, synthRes);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void uploadToS3(String bucketName, String objectKey, InputStream inputStream) {
        try {
            // InputStream을 바이트 배열로 변환
            byte[] bytes = toByteArray(inputStream);

            Map<String, String> metadata = new HashMap<>();
            metadata.put("Content-Type", "audio/mpeg");

            // PutObjectRequest 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .metadata(metadata)
                    .build();

            // 파일을 S3에 업로드
            PutObjectResponse putObjectResponse = s3Client.putObject(
                    putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromBytes(bytes)  // 바이트 배열을 사용하여 업로드
            );

            System.out.println("File uploaded to S3 with ETag: " + putObjectResponse.eTag());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // InputStream을 바이트 배열로 변환하는 헬퍼 메서드
    private byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];  // 16KB 단위로 읽기

        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }
}
