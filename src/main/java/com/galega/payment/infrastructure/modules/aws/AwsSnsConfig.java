package com.galega.payment.infrastructure.modules.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsSnsConfig {

  @Value("${aws.region}")
  private String awsRegion;

  private final AwsCredentialsProvider awsCredentialsProvider;

  public AwsSnsConfig(AwsCredentialsProvider awsCredentialsProvider) {
    this.awsCredentialsProvider = awsCredentialsProvider;
  }

  @Bean(name = "snsClient")
  public SnsClient sqsClient() {
    return SnsClient.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(awsCredentialsProvider)
        .build();
  }

}
