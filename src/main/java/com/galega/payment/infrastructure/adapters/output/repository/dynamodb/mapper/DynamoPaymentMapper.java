package com.galega.payment.infrastructure.adapters.output.repository.dynamodb.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galega.payment.domain.model.Payment;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class DynamoPaymentMapper {

  public static Map<String, AttributeValue> paymentToMap(Payment payment) {

    Map<String, AttributeValue> paymentMap = new HashMap<>();

    paymentMap.put("id", stringAttr(payment.getId().toString()));
    paymentMap.put("externalId", stringAttr(payment.getExternalId()));
    paymentMap.put("orderId", stringAttr(payment.getOrderId().toString()));
    paymentMap.put("amount", numberAttr(payment.getAmount().toString()));
    paymentMap.put("gateway", stringAttr(payment.getGateway()));
    paymentMap.put("status", stringAttr(payment.getStatus()));

    if(payment.getPayedAt() != null) {
      paymentMap.put("payedAt", stringAttr(payment.getPayedAt().toString()));
    }

    if(payment.getTransactionData() != null) {
      paymentMap.put("transactionData", jsonAttr(payment.getTransactionData()));
    }

    return paymentMap;

  }

  public static Payment mapToPayment(Map<String, AttributeValue> paymentMap) {
    Payment payment = new Payment();

    payment.setId(UUID.fromString(paymentMap.get("id").s()));
    payment.setExternalId(paymentMap.get("externalId").s());
    payment.setOrderId(UUID.fromString(paymentMap.get("orderId").s()));
    payment.setAmount(new BigDecimal(paymentMap.get("amount").n()));
    payment.setGateway(paymentMap.get("gateway").s());
    payment.setStatus(paymentMap.get("status").s());

    if(paymentMap.get("payedAt") != null) {
      payment.setStatus(paymentMap.get("payedAt").s());
    }

    if(paymentMap.get("transactionData") != null) {
      String json = paymentMap.get("transactionData").s();
      ObjectMapper mapper = new ObjectMapper();

      try {
        Object data = mapper.readValue(json, Object.class);
        payment.setTransactionData(data);
      }

      catch (JsonProcessingException e) {
        System.out.println("Error parsing json: " + json);
        return payment;
      }

    }

    return payment;

  }

  private static AttributeValue stringAttr(String string) {
    return AttributeValue.builder().s(string).build();
  }

  private static AttributeValue numberAttr(String string) {
    return AttributeValue.builder().n(string).build();
  }

  private static AttributeValue jsonAttr(Object object) {
    ObjectMapper mapper = new ObjectMapper();

    try {
      String json = mapper.writeValueAsString(object);
      return AttributeValue.builder().s(json).build();
    }

    catch (JsonProcessingException e) {
      return AttributeValue.builder().s("").build();
    }
  }



}
