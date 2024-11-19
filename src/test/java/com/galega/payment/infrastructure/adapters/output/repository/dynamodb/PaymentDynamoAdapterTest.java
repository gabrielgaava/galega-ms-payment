package com.galega.payment.infrastructure.adapters.output.repository.dynamodb;

import com.galega.payment.domain.exception.PaymentErrorException;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.infrastructure.adapters.output.repository.dynamodb.mapper.DynamoPaymentMapper;
import com.galega.payment.utils.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentDynamoAdapterTest {

  @Mock
  private DynamoDbClient dynamoDbClient;

  private PaymentDynamoAdapter paymentDynamoAdapter;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    paymentDynamoAdapter = new PaymentDynamoAdapter(dynamoDbClient);
  }

  @Test
  void createOrUpdate_ShouldReturnPayment_WhenSuccessful() throws PaymentErrorException {
    // Mock data
    Payment payment = MockHelper.getCreatedPayment();
    payment.setExternalId("ext123");
    PutItemResponse putItemResponse = mock(PutItemResponse.class);
    DynamoDbResponseMetadata responseMetadata = mock(DynamoDbResponseMetadata.class);

    // Mock behavior
    when(dynamoDbClient.putItem(any(PutItemRequest.class)))
        .thenReturn(putItemResponse);
    when(putItemResponse.responseMetadata()).thenReturn(responseMetadata);
    when(responseMetadata.requestId()).thenReturn("mock-request-id");


    // Execute method
    Payment result = paymentDynamoAdapter.createOrUpdate(payment);

    // Verify interactions and assertions
    assertNotNull(result);
    assertEquals("ext123", result.getExternalId());
    verify(dynamoDbClient).putItem(any(PutItemRequest.class));
  }

  @Test
  void createOrUpdate_ShouldReturnPayment_WhenPayedAtIsNull() throws PaymentErrorException {
    // Mock data
    Payment payment = MockHelper.getCreatedPayment();
    payment.setExternalId("ext123");
    payment.setPayedAt(null);
    payment.setTransactionData(null);
    PutItemResponse putItemResponse = mock(PutItemResponse.class);
    DynamoDbResponseMetadata responseMetadata = mock(DynamoDbResponseMetadata.class);

    // Mock behavior
    when(dynamoDbClient.putItem(any(PutItemRequest.class)))
        .thenReturn(putItemResponse);
    when(putItemResponse.responseMetadata()).thenReturn(responseMetadata);
    when(responseMetadata.requestId()).thenReturn("mock-request-id");


    // Execute method
    Payment result = paymentDynamoAdapter.createOrUpdate(payment);

    // Verify interactions and assertions
    assertNotNull(result);
    assertEquals("ext123", result.getExternalId());
    verify(dynamoDbClient).putItem(any(PutItemRequest.class));
  }

  @Test
  void createOrUpdate_ShouldThrowPaymentErrorException_WhenDynamoDbFails() {
    // Mock data
    Payment payment = MockHelper.getCreatedPayment();
    payment.setExternalId("ext123");

    when(dynamoDbClient.putItem(any(PutItemRequest.class)))
        .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

    // Execute method and assert exception
    assertThrows(PaymentErrorException.class, () -> paymentDynamoAdapter.createOrUpdate(payment));
  }

  @Test
  void findBy_UsingExternalId_ShouldReturnPayment_WhenFound() {

    // Mock data
    Payment expectedPayment = MockHelper.getCreatedPayment();
    expectedPayment.setExternalId("ext123");
    Map<String, AttributeValue> item = DynamoPaymentMapper.paymentToMap(expectedPayment);

    QueryResponse queryResponse = QueryResponse.builder()
        .items(List.of(item))
        .build();

    // Mock behavior
    when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(queryResponse);

    // Execute method
    Payment result = paymentDynamoAdapter.findBy("externalId", "ext123");

    // Assertions
    assertNotNull(result);
    assertEquals("ext123", result.getExternalId());
  }

  @Test
  void findBy_UsingId_ShouldReturnPayment_WhenFound() {

    // Mock data
    Payment expectedPayment = MockHelper.getCreatedPayment();
    Map<String, AttributeValue> item = DynamoPaymentMapper.paymentToMap(expectedPayment);

    QueryResponse queryResponse = QueryResponse.builder()
        .items(List.of(item))
        .build();

    // Mock behavior
    when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(queryResponse);

    // Execute method
    Payment result = paymentDynamoAdapter.findBy("id", expectedPayment.getId().toString());

    // Assertions
    assertNotNull(result);
    assertEquals(expectedPayment.getId(), result.getId());
  }

  @Test
  void findBy_ShouldReturnPayment_WhenThrowJsonProcessingException() {

    // Mock data
    Payment expectedPayment = MockHelper.getCreatedPayment();
    Map<String, AttributeValue> item = DynamoPaymentMapper.paymentToMap(expectedPayment);
    item.put("transactionData", AttributeValue.fromS("invalidJsonString"));

    QueryResponse queryResponse = QueryResponse.builder()
        .items(List.of(item))
        .build();

    // Mock behavior
    when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(queryResponse);

    // Execute method
    Payment result = paymentDynamoAdapter.findBy("id", expectedPayment.getId().toString());

    // Assertions
    assertNotNull(result);
    assertNull(result.getTransactionData());
    assertEquals(expectedPayment.getId(), result.getId());
  }

  @Test
  void findBy_WhenPayedAtIsNull_ShouldReturnPayment_WhenFound() {

    // Mock data
    Payment expectedPayment = MockHelper.getCreatedPayment();
    expectedPayment.setPayedAt(null);
    expectedPayment.setTransactionData(null);
    Map<String, AttributeValue> item = DynamoPaymentMapper.paymentToMap(expectedPayment);

    QueryResponse queryResponse = QueryResponse.builder()
        .items(List.of(item))
        .build();

    // Mock behavior
    when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(queryResponse);

    // Execute method
    Payment result = paymentDynamoAdapter.findBy("id", expectedPayment.getId().toString());

    // Assertions
    assertNotNull(result);
    assertEquals(expectedPayment.getId(), result.getId());
    assertNull(result.getPayedAt());
    assertNull(result.getTransactionData());
  }

  @Test
  void findBy_ShouldReturnNull_WhenNoItemsFound() {
    // Mock behavior
    QueryResponse queryResponse = QueryResponse.builder().items(List.of()).build();
    when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(queryResponse);

    // Execute method
    Payment result = paymentDynamoAdapter.findBy("externalId", "ext123");

    // Assertions
    assertNull(result);
  }

  @Test
  void findBy_ShouldReturnNull_WhenDynamoDbFails() {
    // Mock data
    String randomId = UUID.randomUUID().toString();

    when(dynamoDbClient.query(any(QueryRequest.class)))
        .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

    Payment result = paymentDynamoAdapter.findBy("id", randomId);

    // Execute method and assert exception
    assertNull(result);
  }

  @Test
  void getAll_ShouldReturnPaymentsList_WhenDataExists() {

    // Mock data
    Payment payment1 = MockHelper.getCreatedPayment();
    payment1.setExternalId("ext123");
    Payment payment2 = MockHelper.getCreatedPayment();
    payment2.setExternalId("ext456");

    Map<String, AttributeValue> item1 = DynamoPaymentMapper.paymentToMap(payment1);
    Map<String, AttributeValue> item2 = DynamoPaymentMapper.paymentToMap(payment2);

    ScanResponse scanResponse = ScanResponse.builder()
        .items(List.of(item1, item2))
        .build();

    // Mock behavior
    when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(scanResponse);

    // Execute method
    List<Payment> result = paymentDynamoAdapter.getAll();

    // Assertions
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("ext123", result.get(0).getExternalId());
    assertEquals("ext456", result.get(1).getExternalId());
  }

  @Test
  void getAll_ShouldReturnEmptyList_WhenNoDataExists() {
    // Mock behavior
    ScanResponse scanResponse = ScanResponse.builder().items(List.of()).build();
    when(dynamoDbClient.scan(any(ScanRequest.class))).thenReturn(scanResponse);

    // Execute method
    List<Payment> result = paymentDynamoAdapter.getAll();

    // Assertions
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
