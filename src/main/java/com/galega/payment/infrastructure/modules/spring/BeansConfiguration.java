package com.galega.payment.infrastructure.modules.spring;

import com.galega.payment.domain.service.PaymentService;
import com.galega.payment.infrastructure.adapters.output.repository.dynamodb.PaymentDynamoAdapter;
import com.galega.payment.infrastructure.adapters.output.rest.CustomerApiAdapter;
import com.galega.payment.infrastructure.adapters.output.rest.MercadoPagoAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfiguration {

  @Bean
  public CustomerApiAdapter customerApiAdapter() {
    return new CustomerApiAdapter();
  }

  @Bean
  public MercadoPagoAdapter mercadoPagoAdapter() {
    return new MercadoPagoAdapter();
  }

  @Bean
  public PaymentService paymentService(PaymentDynamoAdapter paymentDynamoAdapter, CustomerApiAdapter customerApiAdapter, MercadoPagoAdapter mercadoPagoAdapter) {
    return new PaymentService(paymentDynamoAdapter, customerApiAdapter, mercadoPagoAdapter);
  }

}
