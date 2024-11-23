package com.galega.payment.infrastructure.adapters.output.notificaiton;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.galega.payment.BaseTestEnv;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.domain.model.payment.PaymentStatus;
import com.galega.payment.infrastructure.adapters.output.notification.SNSHandlerAdapter;
import com.galega.payment.utils.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@SpringBootTest
public class SNSHandlerAdapterTest extends BaseTestEnv {

  @InjectMocks
  private SNSHandlerAdapter snsHandlerAdapter;

  @Mock
  private SnsClient snsClient;

  @BeforeEach
  public void setup() {
    ReflectionTestUtils.setField(
        snsHandlerAdapter,
        "topicArn",
        "arn:aws:sns:us-east-1:111111111111:Fake"
    );
  }

  @Test
  void notifyPaymentApproved_ShouldPublishMessage_WhenValidPayment() throws JsonProcessingException {
    // Arrange
    Payment payment = MockHelper.getCreatedPayment();
    payment.setStatus(PaymentStatus.APPROVED.toString());
    String expectedMessage = this.objectMapper.writeValueAsString(payment);
    PublishResponse mockResponse = PublishResponse.builder()
        .messageId("msg-12345")
        .build();

    when(snsClient.publish(any(PublishRequest.class))).thenReturn(mockResponse);
    ArgumentCaptor<PublishRequest> publishRequestCaptor = ArgumentCaptor.forClass(PublishRequest.class);

    // Act
    snsHandlerAdapter.notifyPaymentApproved(payment);

    // Assert
    verify(snsClient, times(1)).publish(publishRequestCaptor.capture());
    PublishRequest capturedRequest = publishRequestCaptor.getValue();

    assertThat(capturedRequest.topicArn()).isEqualTo("arn:aws:sns:us-east-1:111111111111:Fake");
    assertThat(capturedRequest.message()).isEqualTo(expectedMessage);
    assertThat(capturedRequest.subject()).isEqualTo("PaymentApproved");
  }

  @Test
  void notifyPaymentApproved_ShouldHandleJsonProcessingException() throws JsonProcessingException {

    // Forcing generate a JsonProcessingException error
    try {
      objectMapper.writeValueAsString("{12312321sxx!!C>");
    }

    catch (JsonProcessingException error) {
      // Arrange
      Payment payment = mock(Payment.class);
      doThrow(error).when(objectMapper).writeValueAsString(anyString());

      // Act & Assert
      assertThrows(Exception.class, () -> snsHandlerAdapter.notifyPaymentApproved(payment));
      verify(snsClient, never()).publish(any(PublishRequest.class));
    }

  }

  @Test
  void notifyPaymentApproved_ShouldHandleGenericException() {
    // Arrange
    Payment payment = MockHelper.getCreatedPayment();

    when(snsClient.publish(any(PublishRequest.class))).thenThrow(new RuntimeException("AWS error"));

    // Act
    snsHandlerAdapter.notifyPaymentApproved(payment);

    // Assert
    verify(snsClient, times(1)).publish(any(PublishRequest.class));
  }

}
