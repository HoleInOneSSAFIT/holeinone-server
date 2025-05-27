package com.holeinone.ssafit.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class S3Uploader {

    // application.properties에서 S3 버킷 이름을 주입받음
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // AWS SDK 2.x의 S3 클라이언트 객체
    private final S3Client s3Client;

    // 생성자 주입 방식으로 S3Client 객체를 받음
    public S3Uploader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    //파일 업로드
    public String upload(MultipartFile file, String dirName) throws IOException {
        // 파일명 중복 방지를 위해 UUID를 붙여 고유한 파일명 생성
        String fileName = dirName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // S3에 업로드할 파일의 속성(버킷, 키, 콘텐츠 길이, 콘텐츠 타입)을 지정하는 요청 객체 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket) // 업로드할 버킷 이름
                .key(fileName)  // 버킷 내 저장될 파일 경로 및 이름
                .contentLength(file.getSize()) // 파일 크기 설정
                .contentType(file.getContentType()) // 파일 MIME 타입 설정
                .build();

        // S3에 파일을 업로드함. InputStream과 파일 크기를 RequestBody로 감싸서 전달
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // 업로드된 파일의 URL을 생성하여 반환 (사용자가 접근할 수 있는 URL)
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(fileName)).toString();
    }

    //파일 삭제
    public boolean delete(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            s3Client.deleteObject(builder ->
                    builder.bucket(bucket)
                            .key(key)
                            .build());
            return true;
        } catch (Exception e) {
            // 로그 남기기
            log.error("S3 삭제 실패: {}", e.getMessage());
            return false;
        }
    }

    //파일 경로+파일명 추출
    private String extractKeyFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath(); // /uploaded-videos/..._%EC%98%81%EC%83%81...

            if (path.startsWith("/")) {
                path = path.substring(1); // uploaded-videos/...
            }

            String st = URLDecoder.decode(path, StandardCharsets.UTF_8);

            log.info("파일 key값 : {}", st);

            // URL 디코딩 (한글, 공백 등 복원)
            return st;
        } catch (Exception e) {
            throw new IllegalArgumentException("잘못된 S3 URL입니다: " + fileUrl, e);
        }
    }



}
