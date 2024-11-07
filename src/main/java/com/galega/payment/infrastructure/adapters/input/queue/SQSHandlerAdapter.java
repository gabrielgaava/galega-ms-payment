package com.galega.payment.infrastructure.adapters.input.queue;

import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;

public class SQSHandlerAdapter {

  @Value("${aws.sqs.queue.url}")
  private String queueUrl;

  private final int MAX_NUMBER_MESSAGES = 10;
  private final int WAIT_TIME_SECONDS = 20;
  private SqsClient sqsClient;

  public SQSHandlerAdapter(SqsClient sqsClient) {
    this.sqsClient = sqsClient;
  }

  // Incoming orders with checkout done. Need to proceed to payment gateway
  public void listenQueue() {

    try {
      ReceiveMessageRequest request = ReceiveMessageRequest.builder()
          .queueUrl(queueUrl)
          .maxNumberOfMessages(MAX_NUMBER_MESSAGES)
          .waitTimeSeconds(WAIT_TIME_SECONDS)
          .build();

      ReceiveMessageResponse response = sqsClient.receiveMessage(request);
      List<Message> messages = response.messages();

      for (Message message : messages) {
        System.out.println(message);
      }
    }

    catch (Exception e) {
      System.out.println("Error while receiving messages from SQS queue: ");
    }


  }

}
