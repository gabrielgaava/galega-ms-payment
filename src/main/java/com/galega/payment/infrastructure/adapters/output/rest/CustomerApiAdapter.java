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
  public Customer getCustomerByCPF(String cpf) {
    return this.mockCustomer(cpf);
  }

  @Override
  public Customer getCustomerById(String id) {

    if(isApiMocked) return this.mockCustomer("47667846855");

    WebClient webClient = WebClient.builder()
        .baseUrl(hostApiUrl)
        .build();

    Mono<Customer> response = webClient.get()
        .uri(uriBuilder -> uriBuilder.path("/customers/{id}").build(id))
        .retrieve()
        .bodyToMono(Customer.class)
        .onErrorResume(WebClientResponseException.class, ex -> {
          System.err.println("Erro na requisição: " + ex.getMessage());
          return Mono.empty();
        });

    return response.block();
  }


  private Customer mockCustomer(String cpf) {

    Customer customer = new Customer();

    if(cpf.equals("47667846855")) {
      customer.setCpf("47667846855");
      customer.setId(UUID.fromString("98756d48-ce07-4292-b395-0cbc76f99823"));
      customer.setName("Gabriel Henrique da Silva Gava");
      customer.setEmail("gabriel.gava@gmail.com");

    }

    else {
      customer.setCpf("11111111111");
      customer.setId(UUID.fromString("b3b8c3c1-9f18-43d4-a573-d2b0e5cd2cbc"));
      customer.setName("Cliente não identificado");
      customer.setEmail("unknown@galega.com");
    }

    return customer;

  }
}
