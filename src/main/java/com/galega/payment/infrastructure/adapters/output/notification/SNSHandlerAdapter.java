package com.galega.payment.infrastructure.adapters.output.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.galega.payment.application.ports.output.NotifyPaymentPort;
import com.galega.payment.domain.model.payment.Payment;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Component
public class SNSHandlerAdapter implements NotifyPaymentPort {

  @Value("${aws.sns.topic.arn}")
  private String topicArn;

  private final SnsClient snsClient;
  private final ObjectMapper objectMapper;

  public SNSHandlerAdapter(@Qualifier("snsClient") SnsClient snsClient) {
    this.snsClient = snsClient;
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
    this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Override
  public void notifyPaymentApproved(Payment payment) {

    try {
      String message = objectMapper.writeValueAsString(payment);

      // Cria o PublishRequest com os detalhes da mensagem
      PublishRequest publishRequest = PublishRequest.builder()
          .topicArn(topicArn)
          .message(message)
          .subject("PaymentApproved")
          .build();

      // Envia a mensagem para o tópico
      PublishResponse publishResponse = snsClient.publish(publishRequest);
      System.out.println("Message published! ID: " + publishResponse.messageId());
    }

    catch (JsonProcessingException e) {
      System.err.println("Error while converting payment data to JSON: " + e.getMessage());
    }

    catch (Exception e) {
      System.err.println("Error while publishing to SNS topic: " + e.getMessage());
    }
  }

  @Override
  public void notifyPaymentRefused(Payment payment) {

  }

  @Override
  public void notifyPaymentCanceled(Payment payment) {

  }
}
