package com.galega.payment.infrastructure.modules.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

@Configuration
public class AwsDynamoDbConfig {

  @Value("${aws.region}")
  private String awsRegion;

  private final AwsCredentialsProvider awsCredentialsProvider;

  public AwsDynamoDbConfig(AwsCredentialsProvider awsCredentialsProvider) {
    this.awsCredentialsProvider = awsCredentialsProvider;
  }

  @Bean
  public DynamoDbClient dynamoDbClient() {

    return DynamoDbClient.builder()
      .region(Region.of(awsRegion))
      .credentialsProvider(awsCredentialsProvider)
      .build();

  }

}
