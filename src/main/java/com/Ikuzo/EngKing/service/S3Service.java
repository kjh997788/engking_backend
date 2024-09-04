package com.Ikuzo.EngKing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.stream.Collectors;


@Service
public class S3Service {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Autowired
    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
        this.objectMapper = new ObjectMapper();
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
            return null;
        } catch (Exception e) {
            // 기타 예외 발생 시에도 "null" 반환
            return null;
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


    // S3에 업로드할 수 있는 pre-signed URL 생성 메서드
    public String generateUploadPreSignedUrl(String memberId, String chatRoomId, String messageId) {
        // S3 버킷과 객체 키 설정
        String bucketName = "engking-voice-bucket";
        String objectKey = String.format("audio/%s/%s/%s.mp3", memberId, chatRoomId, messageId);

        // S3Presigner 생성
        S3Presigner presigner = S3Presigner.create();

        // PutObjectRequest 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        // Pre-signed URL 생성 요청 설정
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60)) // URL의 유효 기간 설정
                .putObjectRequest(putObjectRequest)
                .build();

        // Pre-signed URL 생성
        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

        // Pre-signed URL 반환
        return presignedRequest.url().toString();
    }

    // S3에서 JSON 파일을 읽고, 'results.transcripts.transcript' 값을 추출하는 메서드
    public String getTranscriptFromS3(String memberId, String chatRoomId, String messageId) {
        String bucketName = "engking-bucket-dev";
        String objectKey = String.format("audio-%s-%s-%s.json", memberId, chatRoomId, messageId);

        try {
            // S3에서 JSON 파일 가져오기
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

            // JSON 파일 내용 읽기
            String jsonContent = new BufferedReader(new InputStreamReader(s3Object))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // JSON 파싱하여 'transcript' 추출
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            JsonNode transcriptsNode = rootNode.path("results").path("transcripts");

            if (transcriptsNode.isArray() && transcriptsNode.size() > 0) {
                return transcriptsNode.get(0).path("transcript").asText();
            }

        } catch (NoSuchKeyException e) {
            System.err.println("No such key in bucket: " + objectKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
