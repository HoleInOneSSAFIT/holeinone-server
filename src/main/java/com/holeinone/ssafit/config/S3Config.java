package com.holeinone.ssafit.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Spring이 설정 클래스로 인식하게 된다
@Configuration
public class S3Config {

    //application.properties에 정의된 값을 주입받음
    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;

    // Bean으로 등록하여 다른 곳에서 주입받아 사용할 수 있게 함
    @Bean
    public AmazonS3 s3Client() {

        // 액세스 키와 시크릿 키를 기반으로 자격 증명 생성
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

        // Amazon S3 클라이언트를 생성하고 리턴
        return AmazonS3ClientBuilder.standard()
                .withRegion(region) // AWS 리전 설정
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials)) // 자격 증명 설정
                .build();
    }
}
