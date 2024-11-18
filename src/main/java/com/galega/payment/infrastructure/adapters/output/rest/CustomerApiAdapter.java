package com.galega.payment.infrastructure.adapters.output.rest;

import com.galega.payment.application.ports.output.CustomerPort;
import com.galega.payment.domain.model.customer.Customer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class CustomerApiAdapter implements CustomerPort {

  @Value("${aws.eks.service.customer}")
  String hostApiUrl;

  @Value("${mock.api}")
  Boolean isApiMocked;

  @Override
  public Customer getCustomerById(String id) {

    if(Boolean.TRUE.equals(isApiMocked)) return this.mockCustomer();

    WebClient webClient = WebClient.builder()
        .baseUrl(hostApiUrl)
        .build();

    Mono<Customer> response = webClient.get()
        .uri(uriBuilder -> uriBuilder.path("/customer/{id}").build(id))
        .retrieve()
        .bodyToMono(Customer.class)
        .onErrorResume(WebClientResponseException.class, ex -> {
          System.err.println("Erro na requisição: " + ex.getMessage());
          return Mono.empty();
        });

    return response.block();
  }

  private Customer mockCustomer() {

    Customer customer = new Customer();
    customer.setCpf("47667846855");
    customer.setId(UUID.fromString("98756d48-ce07-4292-b395-0cbc76f99823"));
    customer.setName("Gabriel Henrique da Silva Gava");
    customer.setEmail("gabriel.gava@gmail.com");

    return customer;

  }
}
