package com.holeinone.ssafit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration  // Spring이 이 클래스를 설정 클래스임을 인식하도록 함
public class S3Config {

    // application.properties에서 AWS 리전 값을 주입받음
    @Value("${cloud.aws.region.static}")
    private String region;

    // AWS 액세스 키를 주입받음
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    // AWS 시크릿 키를 주입받음
    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    /**
     * S3Client를 Bean으로 등록하여 다른 클래스에서 주입받아 사용할 수 있게 함
     * @return AWS SDK 2.x의 S3Client 객체
     */
    @Bean
    public S3Client s3Client() {
        // AWS 자격 증명 생성 (액세스 키, 시크릿 키)
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        // S3 클라이언트 빌더를 사용해 리전 및 자격 증명 설정 후 빌드
        return S3Client.builder()
                .region(Region.of(region)) // AWS 리전 설정
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds)) // 자격 증명 설정
                .build();
    }
}
