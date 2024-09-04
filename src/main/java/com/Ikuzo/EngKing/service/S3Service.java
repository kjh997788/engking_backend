package com.Ikuzo.EngKing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Autowired
    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    // 음성 파일에 대한 pre-signed url 생성
    public String generatePreSignedUrl(String memberId, String chatRoomId, String messageId) {
        // 객체의 키 경로 생성
        String bucketName = "engking-voice-bucket";
        String objectKey = String.format("audio/%s/%s/%s.mp3", memberId, chatRoomId, messageId);

        // 객체 존재 여부 확인
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            HeadObjectResponse headObjectResponse = s3Client.headObject(headObjectRequest);

        } catch (NoSuchKeyException e) {
            // 객체가 존재하지 않으면 "null" 반환
            return "null";
        } catch (Exception e) {
            // 기타 예외 발생 시에도 "null" 반환
            return "null";
        }

        // 객체가 존재하면 S3Presigner를 사용하여 프리사인드 URL 생성
        S3Presigner presigner = S3Presigner.create();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60)) // URL의 유효 기간 설정
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(getObjectPresignRequest);

        // 프리사인드 URL 반환
        return presignedRequest.url().toString();
    }
}
