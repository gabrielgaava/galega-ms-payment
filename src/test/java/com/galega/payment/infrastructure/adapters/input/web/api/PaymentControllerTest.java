package com.galega.payment.infrastructure.adapters.input.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galega.payment.BaseTestEnv;
import com.galega.payment.application.ports.input.CreatePaymentUseCase;
import com.galega.payment.application.ports.input.GetPaymentUseCase;
import com.galega.payment.domain.model.order.Order;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.infrastructure.adapters.input.queue.SQSHandlerAdapter;
import com.galega.payment.utils.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest extends BaseTestEnv {

  @Mock
  private CreatePaymentUseCase createPaymentUseCase;

  @Mock
  private GetPaymentUseCase getPaymentUseCase;

  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders
        .standaloneSetup(new PaymentController(createPaymentUseCase, getPaymentUseCase))
        .build();
  }

  @Test
  void createPayment_ShouldReturnCreatedPayment() throws Exception {

    // Given
    Order order = MockHelper.getCreatedOrder();
    Payment payment = MockHelper.getCreatedPayment();

    // When
    when(createPaymentUseCase.createPayment(any(Order.class))).thenReturn(payment);

    // Then
    mockMvc.perform(MockMvcRequestBuilders
            .post("/payments")
            .content(objectMapper.writeValueAsString(order))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.amount").value(payment.getAmount()))
        .andExpect(jsonPath("$.status").value(payment.getStatus()));
    verify(createPaymentUseCase, times(1)).createPayment(any(Order.class));
  }

  @Test
  void getAllPayments_ShouldReturnListOfPayments() throws Exception {

    // Given
    Payment payment1 = MockHelper.getCreatedPayment();
    Payment payment2 = MockHelper.getCreatedPayment();

    // When
    when(getPaymentUseCase.getAllPayments()).thenReturn(Arrays.asList(payment1, payment2));

    // Then
    mockMvc.perform(get("/payments"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").exists())
        .andExpect(jsonPath("$[0].amount").value(payment1.getAmount()))
        .andExpect(jsonPath("$[1].status").value(payment2.getStatus()));

    verify(getPaymentUseCase, times(1)).getAllPayments();
  }

  @Test
  void getPayment_ShouldReturnPaymentById() throws Exception {

    // Given
    Payment payment = MockHelper.getCreatedPayment();
    String paymentId = payment.getId().toString();

    when(getPaymentUseCase.getByPaymentId(paymentId)).thenReturn(payment);

    // When and Then
    mockMvc.perform(get("/payments/{id}", paymentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(paymentId))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()))
        .andExpect(jsonPath("$.status").value(payment.getStatus()));

    verify(getPaymentUseCase, times(1)).getByPaymentId(paymentId);
  }

  @Test
  void getPayment_ShouldReturnPaymentByExternalId_WhenIsExternalIsTrue() throws Exception {

    // Given
    Payment payment = MockHelper.getCreatedPayment();
    String externalId = payment.getExternalId();

    when(getPaymentUseCase.getByPaymentExternalId(externalId)).thenReturn(payment);

    // When and Then
    mockMvc.perform(get("/payments/{id}", externalId).param("isExternal", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.externalId").value(payment.getExternalId()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()))
        .andExpect(jsonPath("$.status").value(payment.getStatus()));

    verify(getPaymentUseCase, times(1)).getByPaymentExternalId(externalId);
  }

  @Test
  void getPayment_ShouldReturnPaymentById_WhenIsExternalIsFalse() throws Exception {

    // Given
    Payment payment = MockHelper.getCreatedPayment();
    String id = payment.getId().toString();

    when(getPaymentUseCase.getByPaymentId(id)).thenReturn(payment);

    // When and Then
    mockMvc.perform(get("/payments/{id}", id).param("isExternal", "false"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.externalId").value(payment.getExternalId()))
        .andExpect(jsonPath("$.amount").value(payment.getAmount()))
        .andExpect(jsonPath("$.status").value(payment.getStatus()));

    verify(getPaymentUseCase, times(1)).getByPaymentId(id);
  }

  @Test
  void createPaymentFake_ShouldReturnFakePayment() throws Exception {

    // Given
    Order order = MockHelper.getCreatedOrder();
    Payment payment = MockHelper.getCreatedPayment();

    when(createPaymentUseCase.createPayment(any(Order.class))).thenReturn(payment);

    // When and Then
    mockMvc.perform(post("/payments/fake-checkout")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(order)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.amount").value(payment.getAmount()))
        .andExpect(jsonPath("$.status").value(payment.getStatus()));

    verify(createPaymentUseCase, times(1)).createPayment(any(Order.class));
  }
}
