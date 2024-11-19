package com.galega.payment.infrastructure.adapters.output.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.galega.payment.domain.model.customer.Customer;
import com.galega.payment.utils.MockHelper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomerApiAdapterTest {

  private CustomerApiAdapter customerApiAdapter;

  private MockWebServer mockWebServer;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    customerApiAdapter = new CustomerApiAdapter();
    customerApiAdapter.hostApiUrl = mockWebServer.url("/").toString();
    customerApiAdapter.isApiMocked = Boolean.FALSE;
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }


  @Test
  void getCustomerById_ShouldReturnCustomer_WhenApiReturnsData() throws JsonProcessingException {

    // Configurando resposta mockada do WebClient
    Customer mockCustomer = MockHelper.getUnknownCustomer();
    MockResponse mockResponse = MockHelper.getMockJsonResponse(mockCustomer);
    mockWebServer.enqueue(mockResponse);

    Customer customer = customerApiAdapter.getCustomerById("123");

    assertNotNull(customer);
    assertThat(customer).usingRecursiveComparison().isEqualTo(mockCustomer);
  }

  @Test
  void getCustomerById_ShouldReturnNull_WhenApiThrowsError() throws JsonProcessingException {

    // Configurando resposta mockada do WebClient
    MockResponse mockResponse = MockHelper.getMockJsonResponse(new Customer());
    mockResponse.setResponseCode(404);
    mockResponse.setBody("");
    mockWebServer.enqueue(mockResponse);

    // Testando a chamada
    Customer result = customerApiAdapter.getCustomerById("non-existing-id");

    assertNull(result, "Expected null when API throws error");
  }

  @Test
  void getCustomerById_ShouldReturnMockedCustomer_WhenMockIsEnabled() {
    // Ativando mock na classe
    customerApiAdapter.isApiMocked = true;

    // Testando a chamada
    Customer result = customerApiAdapter.getCustomerById("any-id");

    assertNotNull(result);
    assertEquals("98756d48-ce07-4292-b395-0cbc76f99823", result.getId().toString());
    assertEquals("47667846855", result.getCpf());
    assertEquals("Gabriel Henrique da Silva Gava", result.getName());
    assertEquals("gabriel.gava@gmail.com", result.getEmail());
  }
}

