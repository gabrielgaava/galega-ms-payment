package com.galega.payment.infrastructure.modules.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
public class AwsSqsConfig {

  @Value("${aws.region}")
  private String awsRegion;

  private final AwsCredentialsProvider awsCredentialsProvider;

  public AwsSqsConfig(AwsCredentialsProvider awsCredentialsProvider) {
    this.awsCredentialsProvider = awsCredentialsProvider;
  }

  @Bean
  public SqsClient sqsClient() {
    return SqsClient.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(awsCredentialsProvider)
        .build();
  }

}
