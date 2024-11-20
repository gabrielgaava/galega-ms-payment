package com.galega.payment.infrastructure.modules.spring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.galega.payment.domain.service.PaymentService;
import com.galega.payment.infrastructure.adapters.output.notification.SNSHandlerAdapter;
import com.galega.payment.infrastructure.adapters.output.repository.dynamodb.PaymentDynamoAdapter;
import com.galega.payment.infrastructure.adapters.output.rest.CustomerApiAdapter;
import com.galega.payment.infrastructure.adapters.output.rest.MercadoPagoAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfiguration {

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }

  @Bean
  public CustomerApiAdapter customerApiAdapter() {
    return new CustomerApiAdapter();
  }

  @Bean
  public MercadoPagoAdapter mercadoPagoAdapter() {
    return new MercadoPagoAdapter();
  }

  @Bean
  public PaymentService paymentService(
      PaymentDynamoAdapter paymentDynamoAdapter,
      CustomerApiAdapter customerApiAdapter,
      MercadoPagoAdapter mercadoPagoAdapter,
      SNSHandlerAdapter snsHandlerAdapter
  ) {
    return new PaymentService(paymentDynamoAdapter, customerApiAdapter, mercadoPagoAdapter, snsHandlerAdapter);
  }

}
