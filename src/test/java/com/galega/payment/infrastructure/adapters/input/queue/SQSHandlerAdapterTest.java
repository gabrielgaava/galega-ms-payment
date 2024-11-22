package com.galega.payment.infrastructure.adapters.input.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.galega.payment.BaseTestEnv;
import com.galega.payment.application.ports.input.CreatePaymentUseCase;
import com.galega.payment.domain.model.order.Order;
import com.galega.payment.utils.MockHelper;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.codehaus.plexus.archiver.tar.TarLongFileMode.fail;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SQSHandlerAdapterTest {

  private SqsClient sqsClient;
  private CreatePaymentUseCase createPaymentUseCase;
  private ObjectMapper mapper;
  private SQSHandlerAdapter sqsHandlerAdapter;
  private static final String QUEUE_URL = "https://sqs.aws-region.amazonaws.com/dummy/test-queue";
  ReceiveMessageResponse response = ReceiveMessageResponse.builder()
      .messages(Collections.emptyList())
      .build();

  @BeforeEach
  void setup() {
    // Mock SqsClient
    sqsClient = Mockito.mock(SqsClient.class);

    // Mock CreatePaymentUseCase
    createPaymentUseCase = Mockito.mock(CreatePaymentUseCase.class);

    // Configura o ObjectMapper
    mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // Instancia a classe a ser testada
    sqsHandlerAdapter = new SQSHandlerAdapter(sqsClient, createPaymentUseCase);
    ReflectionTestUtils.setField(sqsHandlerAdapter, "queueUrl", QUEUE_URL);
    ReflectionTestUtils.setField(sqsHandlerAdapter, "listenerEnabled", true);
    response = ReceiveMessageResponse.builder()
        .messages(Collections.emptyList())
        .build();
  }

  @Test
  void listenQueue_ShouldProcessMessages_WhenMessagesAreReceived() throws InterruptedException, JsonProcessingException {

    Order order1 = MockHelper.getCreatedOrder();
    Order order2 = MockHelper.getCreatedOrder();
    String order1Jsons = mapper.writeValueAsString(order1);
    String order2Jsons = mapper.writeValueAsString(order2);

    // Simulate the messages sent to the queue
    Message message1 = Message.builder().messageId("001").body(order1Jsons).build();
    Message message2 = Message.builder().messageId("002").body(order2Jsons).build();

    ReceiveMessageResponse response = ReceiveMessageResponse.builder()
        .messages(List.of(message1, message2))
        .build();

    when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(response).thenReturn(this.response);
    when(sqsClient.deleteMessage(any(DeleteMessageRequest.class))).thenReturn(DeleteMessageResponse.builder().build());

    // Controls the Thread runtime
    CountDownLatch latch = new CountDownLatch(1);

    // Create a Thread to run the Listening Queue service
    Thread listeningThread = new Thread(() -> {
      try {
        sqsHandlerAdapter.listenQueue();
      }

      catch (Exception e) {
        fail("Should procces the message");
      }

      finally {
        latch.countDown();
        sqsHandlerAdapter.stopListening();
      }
    });

    // Start the thread
    listeningThread.start();

    // Wait the thread run a second
    latch.await(1, TimeUnit.SECONDS);

    // Check how many times and messages was processed by the handler
    verify(createPaymentUseCase, times(2)).createPayment(any(Order.class));
    verify(sqsClient, times(2)).deleteMessage(any(DeleteMessageRequest.class));
  }

}
