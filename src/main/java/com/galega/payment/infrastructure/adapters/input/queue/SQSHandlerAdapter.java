package com.galega.payment.infrastructure.adapters.input.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galega.payment.application.ports.input.CreatePaymentUseCase;
import com.galega.payment.domain.model.order.Order;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SQSHandlerAdapter {

  @Value("${aws.sqs.queue.url}")
  private String queueUrl;

  private final int MAX_NUMBER_MESSAGES = 5;
  private final int WAIT_TIME_SECONDS = 20;

  private final SqsClient sqsClient;
  private final ExecutorService executorService;
  private final CreatePaymentUseCase createPaymentUseCase;

  public SQSHandlerAdapter(@Qualifier("sqsClient") SqsClient sqsClient, CreatePaymentUseCase createPaymentUseCase) {
    this.sqsClient = sqsClient;
    this.createPaymentUseCase = createPaymentUseCase;
    this.executorService = Executors.newSingleThreadExecutor();
  }


  @PostConstruct
  public void startListening() {
    // Submete o método para execução contínua
    System.out.println("Listening Queue: " + queueUrl);
    executorService.submit(this::listenQueue);
  }

  @PreDestroy
  public void stopListening() {
    // Finaliza o listener ao encerrar a aplicação
    System.out.println("Listening Queue Stopped");
    executorService.shutdownNow();
  }

  // Incoming orders with checkout done. Need to proceed to payment gateway
  public void listenQueue() {

    // Loop contínuo até ser interrompido
    while (!Thread.currentThread().isInterrupted()) {

      try {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(MAX_NUMBER_MESSAGES)
            .waitTimeSeconds(WAIT_TIME_SECONDS)
            .build();

        ReceiveMessageResponse response = sqsClient.receiveMessage(request);
        List<Message> messages = response.messages();

        messages.forEach(this::processMessage);
      }

      catch (SqsException e) {
        System.err.println("Error while receiving messages from SQS queue: " + e.getMessage());
      }

    }

  }

  private void processMessage(Message message) {
    try {

      System.out.println("Processing message: " + message.messageId());

      try {
        ObjectMapper objectMapper = new ObjectMapper();
        Order order = objectMapper.readValue(message.body(), Order.class);
        createPaymentUseCase.createPayment(order);
      }

      catch (JsonProcessingException e) {
        System.err.println("Failed to process messagem. Invalid JSON: " + message.body());
      }

      // Após processar, remova a mensagem da fila
      deleteMessageFromQueue(message);
    }

    catch (SqsException e) {
      System.err.println("Failed to process message: " + message.body());
    }
  }

  private void deleteMessageFromQueue(Message message) {

    try {
      DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest
          .builder()
          .queueUrl(queueUrl)
          .receiptHandle(message.receiptHandle())
          .build();

      sqsClient.deleteMessage(deleteMessageRequest);
    }

    catch (SqsException e) {
      System.err.println(e.awsErrorDetails().errorMessage());
    }

  }

}
