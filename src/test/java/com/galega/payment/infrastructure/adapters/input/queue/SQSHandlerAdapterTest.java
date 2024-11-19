package com.galega.payment.infrastructure.adapters.input.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SQSHandlerAdapterTest {

  private SqsClient sqsClient;
  private SQSHandlerAdapter sqsHandlerAdapter;

  @BeforeEach
  void setup() {
    sqsClient = Mockito.mock(SqsClient.class);
    sqsHandlerAdapter = new SQSHandlerAdapter(sqsClient);
    // Define o valor da URL da fila via Reflection, já que @Value não é processada em testes
    ReflectionTestUtils.setField(sqsHandlerAdapter, "queueUrl", "https://sqs.us-east-1.amazonaws.com/123456789012/my-queue");
  }

  @Test
  void listenQueue_ShouldProcessMessages_WhenMessagesAreReceived() {
    // Simula mensagens recebidas
    Message message1 = Message.builder().body("Message 1").build();
    Message message2 = Message.builder().body("Message 2").build();

    ReceiveMessageResponse response = ReceiveMessageResponse.builder()
        .messages(List.of(message1, message2))
        .build();

    when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(response);

    // Executa o método
    sqsHandlerAdapter.listenQueue();

    // Verifica se as mensagens foram recebidas e processadas
    verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
    // Podemos incluir asserts adicionais se houver mais lógica de processamento de mensagens
  }

  @Test
  void listenQueue_ShouldHandleException_WhenErrorOccurs() {
    // Simula uma exceção ao tentar receber mensagens
    when(sqsClient.receiveMessage(any(ReceiveMessageRequest.class))).thenThrow(new RuntimeException("SQS error"));

    // Executa o método
    sqsHandlerAdapter.listenQueue();

    // Verifica se o método foi chamado e lidou com a exceção
    verify(sqsClient).receiveMessage(any(ReceiveMessageRequest.class));
    // Nenhuma exceção deve ser propagada
  }
}
