package com.galega.payment.infrastructure.adapters.output.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.galega.payment.application.ports.output.PaymentGatewayPort;
import com.galega.payment.domain.exception.PaymentErrorException;
import com.galega.payment.domain.model.payment.CheckoutMessage;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.domain.model.payment.PaymentStatus;
import com.galega.payment.domain.model.payment.PixTransactionalData;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.common.IdentificationRequest;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.PaymentTransactionData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MercadoPagoAdapter implements PaymentGatewayPort {

  @Value("${mercadopago.access.key}")
  private String accessKey;

  @Value("${mercadopago.public.key}")
  private String publicKey;

  @Value("${mercadopago.debug.log}")
  private Boolean isDebugOn;

  private final String gatewayName = "MercadoPago";

  /**
   * This method request a Payment to Mercado Pago and store the information on database
   * @param checkoutMessage: The message coming from queue service requesting the payment
   * @return The created Payment
   * **/
  @Override
  public Payment requestPayment(CheckoutMessage checkoutMessage) throws PaymentErrorException {

    MercadoPagoConfig.setAccessToken(accessKey);
    PaymentClient mpClient = new PaymentClient();

    MPRequestOptions requestHeaders = this.getMPRequestOptions(checkoutMessage);
    PaymentCreateRequest request = this.createPaymentRequest(checkoutMessage);

    this.debugMercadoPago(requestHeaders, request);

    try {
      var response = mpClient.create(request, requestHeaders);

      if(response == null){
        throw new PaymentErrorException(checkoutMessage.getOrderId(), gatewayName);
      }

      Payment payment = new Payment();
      payment.setGateway(gatewayName);
      payment.setId(UUID.randomUUID());
      payment.setExternalId(String.valueOf(response.getId()));
      payment.setStatus(PaymentStatus.PENDING.toString());
      payment.setAmount(new BigDecimal(checkoutMessage.getOrderAmount()));
      payment.setPayedAt(null);
      payment.setOrderId(UUID.fromString(checkoutMessage.getOrderId()));

      PixTransactionalData transactionalData = getPixTransactionalData(response);

      payment.setTransactionData(transactionalData);
      return payment;
    }

    catch (MPException e) {
      System.out.println(e.getMessage());
      throw new PaymentErrorException(checkoutMessage.getOrderId(), gatewayName);
    }

    catch (MPApiException e) {
      var response = e.getApiResponse();
      System.out.println(response.getContent());
      throw new PaymentErrorException(checkoutMessage.getOrderId(), gatewayName);
    }

  }

  private static PixTransactionalData getPixTransactionalData(com.mercadopago.resources.payment.Payment response) {
    PixTransactionalData transactionalData = new PixTransactionalData();
    PaymentTransactionData responseData = response.getPointOfInteraction().getTransactionData();

    transactionalData.setTransactionId(responseData.getTransactionId());
    transactionalData.setPaymentLink(responseData.getTicketUrl());
    transactionalData.setQrCode(responseData.getQrCode());
    transactionalData.setQrCodeBase64(responseData.getQrCodeBase64());
    transactionalData.setBillingDate(responseData.getBillingDate());
    return transactionalData;
  }

  /**
   * This method get a Payment from Mercado Pago API
   * @param payment: The internal payment tha had some change by notification
   * @return The payment entity updated
   * **/
  @Override
  public Payment handlePaymentUpdate(Payment payment) throws PaymentErrorException {

    MercadoPagoConfig.setAccessToken(accessKey);
    PaymentClient mpClient = new PaymentClient();

    try {
      var response = mpClient.get(Long.valueOf(payment.getExternalId()));
      var updateStatus = response.getStatus();

      // Payment Was Approved
      if(updateStatus.equals(com.mercadopago.resources.payment.PaymentStatus.APPROVED)){
        payment.setStatus(PaymentStatus.APPROVED.toString());
        payment.setPayedAt(LocalDateTime.now());
        return payment;
      }

      // Payment Was Reject
      if(updateStatus.equals(com.mercadopago.resources.payment.PaymentStatus.REJECTED)){
        payment.setStatus(PaymentStatus.REFUSED.toString());
        payment.setPayedAt(null);
      }

      // Payment Was Cancelled by the user
      if(updateStatus.equals(com.mercadopago.resources.payment.PaymentStatus.CANCELLED)){
        payment.setStatus(PaymentStatus.CANCELLED.toString());
        payment.setPayedAt(null);
      }

      return payment;
    }

    catch (MPException | MPApiException e) {
      throw new PaymentErrorException(payment.getExternalId(), gatewayName);
    }
  }

  @Override
  public Payment fakeHandlePayment(Payment payment) {
    payment.setStatus(PaymentStatus.APPROVED.toString());
    payment.setPayedAt(LocalDateTime.now());
    return payment;
  }

  /**
   * Build header object for request
   * @param message: The message coming from queue service requesting the payment
   * @return The built object with headers set
   * **/
  private MPRequestOptions getMPRequestOptions(CheckoutMessage message) {
    Map<String, String> customHeaders = new HashMap<>();
    System.out.println("Order ID > " + message.getOrderId());
    customHeaders.put("x-idempotency-key", message.getOrderId());

    return MPRequestOptions.builder()
        .customHeaders(customHeaders)
        .build();
  }

  /**
   * Build object for body request
   * @param message: The message coming from queue service requesting the payment
   * @return The built object for body request to mercado pago API
   * **/
  private PaymentCreateRequest createPaymentRequest(CheckoutMessage message) {
    PaymentPayerRequest payer = null;

    if(message.getCustomerName() != null) {
      String[] names = message.getCustomerName().split(" ");
      String firstName = names[0];
      String lastName = names[names.length - 1];

      payer = PaymentPayerRequest.builder()
          .email(message.getCustomerEmail())
          .firstName(firstName)
          .lastName(lastName)
          .identification(IdentificationRequest.builder().type("CPF").number(message.getCustomerDocument()).build())
          .build();
    }

    return PaymentCreateRequest.builder()
        .transactionAmount(new BigDecimal(message.getOrderAmount()))
        .description("Pedido Galegaburger " + message.getOrderId())
        .paymentMethodId("pix")
        .dateOfExpiration(OffsetDateTime.now().plusHours(24))
        .payer(payer)
        .build();
  }

  /**
   * Just log the input and output from API
   * @param requestHeaders: The built object with headers set
   * @param request: The built object for body request to mercado pago API
   * **/
  private void debugMercadoPago(MPRequestOptions requestHeaders, PaymentCreateRequest request) {
    if(isDebugOn) {
      try {
        ObjectMapper mapper = Jackson2ObjectMapperBuilder.json()
            .modules(new JavaTimeModule())
            .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .build();

        System.out.println("Headers: " + mapper.writeValueAsString(requestHeaders));
        System.out.println("Request: " + mapper.writeValueAsString(request));
      }

      catch (JsonProcessingException e) {
        System.out.println(e.getMessage());
      }
    }
  }

}
