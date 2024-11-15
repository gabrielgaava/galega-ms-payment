package com.galega.payment.infrastructure.adapters.output.repository.dynamodb;

import com.galega.payment.application.ports.output.PaymentRepositoryPort;
import com.galega.payment.domain.exception.PaymentErrorException;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.infrastructure.adapters.output.repository.dynamodb.mapper.DynamoPaymentMapper;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PaymentDynamoAdapter implements PaymentRepositoryPort {

  private final String TABLE_NAME = "Payment";

  private final DynamoDbClient dynamoDbClient;

  public PaymentDynamoAdapter(DynamoDbClient dynamoDbClient) {
    this.dynamoDbClient = dynamoDbClient;
  }

  /**
   * Create a new payment document in DynamoDB if the ID do not exist. Otherwise, create a new register with the
   * informed ID and data
   * @param payment The complete object of the Payment that will be stored. ID and ExternalID is required
   * @return The stored Payment data
   * **/
  @Override
  public Payment createOrUpdate(Payment payment) throws PaymentErrorException {

    Map<String, AttributeValue> paymentItem = DynamoPaymentMapper.paymentToMap(payment);

    PutItemRequest request = PutItemRequest.builder()
        .tableName(TABLE_NAME)
        .item(paymentItem)
        .build();

    try {
      PutItemResponse response = dynamoDbClient.putItem(request);
      System.out.println(TABLE_NAME + " was successfully updated. The request id is "
          + response.responseMetadata().requestId());

      return payment;
    }

    catch (DynamoDbException e) {
      System.out.println(e.getMessage());
      throw new PaymentErrorException(payment.getExternalId(), "MercadoPago");
    }


  }

  /**
   * Find a Payment register by its ID
   * @param key: The key used to search on database (Must be the Primary Key or Sort Key)
   * @param value: The string value of the key
   * @return The Payment object if it was found, or null, otherwise.
   * **/
  @Override
  public Payment findBy(String key, String value) {

    String expression = ":" + key;
    String conditionExpression = key + " = " + expression;

    Map<String, AttributeValue> keyCondition  = new HashMap<>();
    keyCondition .put(expression, AttributeValue.builder().s(value).build());

    QueryRequest queryRequest = QueryRequest .builder()
        .tableName(TABLE_NAME)
        .keyConditionExpression(conditionExpression)
        .expressionAttributeValues(keyCondition)
        .build();

    try {
      QueryResponse response = this.dynamoDbClient.query(queryRequest);
      List<Map<String, AttributeValue>> items = response.items();

      if(items.isEmpty()) return null;

      Map<String, AttributeValue> item = items.getFirst();
      return DynamoPaymentMapper.mapToPayment(item);

    }

    catch (DynamoDbException e) {
      System.out.println(e.getMessage());
      return null;
    }

  }

  /**
   * Retrieve all register from DynamoDB table
   * @return The list of payments stored in DynamoDB
   * **/
  @Override
  public List<Payment> getAll() {

    ScanRequest scanRequest = ScanRequest.builder().tableName(TABLE_NAME).build();
    ScanResponse response = this.dynamoDbClient.scan(scanRequest);

    List<Map<String, AttributeValue>> items = response.items();

    if(items.isEmpty()) {
      return new ArrayList<>();
    }

    List<Payment> payments = new ArrayList<>();

    for(Map<String, AttributeValue> item : items) {
      Payment payment = DynamoPaymentMapper.mapToPayment(item);
      payments.add(payment);
    }

    return payments;
  }

}
