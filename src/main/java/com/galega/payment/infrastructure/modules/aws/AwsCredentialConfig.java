package com.galega.payment.infrastructure.modules.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Configuration
public class AwsCredentialConfig {

  @Value("${aws.access.key}")
  private String accessKey;

  @Value("${aws.secret.key}")
  private String secretKey;

  @Value("${aws.session.token}")
  private String sessionToken;

  @Bean
  public AwsCredentialsProvider awsCredentialsProvider() {
    AwsSessionCredentials awsSessionCredentials = AwsSessionCredentials.create(accessKey, secretKey, sessionToken);
    return StaticCredentialsProvider.create(awsSessionCredentials);
  }

}
