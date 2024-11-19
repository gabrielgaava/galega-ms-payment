package com.galega.payment.infrastructure.adapters.output.rest;

import com.galega.payment.domain.exception.PaymentErrorException;
import com.galega.payment.domain.model.payment.CheckoutMessage;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.domain.model.payment.PaymentStatus;
import com.galega.payment.utils.MockHelper;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.net.MPResponse;
import com.mercadopago.resources.payment.PaymentPointOfInteraction;
import com.mercadopago.resources.payment.PaymentTransactionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MercadoPagoAdapterTest {

  @InjectMocks
  private MercadoPagoAdapter mercadoPagoAdapter;

  @Mock
  private PaymentClient paymentClient;

  private CheckoutMessage checkoutMessage;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    paymentClient = mock(PaymentClient.class);
    mercadoPagoAdapter = new MercadoPagoAdapter(paymentClient);

    // Mock do CheckoutMessage
    checkoutMessage = new CheckoutMessage();
    checkoutMessage.setOrderId(UUID.randomUUID().toString());
    checkoutMessage.setOrderAmount("100.00");
    checkoutMessage.setCustomerName("Gabriel Henrique");
    checkoutMessage.setCustomerEmail("gabriel@galega.com");
    checkoutMessage.setCustomerDocument("12345678901");

  }

  @Test
  void testRequestPayment_successful() throws Exception {
    // Mock da resposta do Payment
    ReflectionTestUtils.setField(mercadoPagoAdapter,"isDebugOn", true);
    com.mercadopago.resources.payment.Payment mockResponse = mock(com.mercadopago.resources.payment.Payment.class);
    when(mockResponse.getId()).thenReturn(12345L);

    PaymentTransactionData transactionData = mock(PaymentTransactionData.class);
    when(transactionData.getQrCode()).thenReturn("mock-qr-code");
    when(transactionData.getTicketUrl()).thenReturn("mock-url");

    var poi = mock(PaymentPointOfInteraction.class);
    when(poi.getTransactionData()).thenReturn(transactionData);
    when(mockResponse.getPointOfInteraction()).thenReturn(poi);

    when(paymentClient.create(any(PaymentCreateRequest.class), any(MPRequestOptions.class))).thenReturn(mockResponse);

    // Execução do método
    Payment result = mercadoPagoAdapter.requestPayment(checkoutMessage);

    // Verificações
    assertNotNull(result);
    assertEquals(PaymentStatus.PENDING.toString(), result.getStatus());
    assertEquals(new BigDecimal("100.00"), result.getAmount());
    assertEquals("mock-url", result.getTransactionData().getPaymentLink());
  }

  @Test
  void testRequestPayment_nullResponse_throwsMPException() throws MPException, MPApiException {
    // Simula resposta nula do PaymentClient
    when(paymentClient.create(
        any(PaymentCreateRequest.class),
        any(MPRequestOptions.class))
    ).thenThrow(new MPException("Erro MP"));

    // Execução e verificação
    assertThrows(PaymentErrorException.class, () -> mercadoPagoAdapter.requestPayment(checkoutMessage));
  }

  @Test
  void testRequestPayment_nullResponse_throwsMPApiException() throws MPException, MPApiException {

    Map<String, List<String>> fakeHeaders = new HashMap<>();
    CheckoutMessage messageWithoutCustomer = checkoutMessage;
    messageWithoutCustomer.setCustomerName(null);

    // Simula resposta nula do PaymentClient
    when(paymentClient.create(
        any(PaymentCreateRequest.class),
        any(MPRequestOptions.class))
    ).thenThrow(new MPApiException("Erro", new MPResponse(500, fakeHeaders, "Erro")));

    // Execução e verificação
    assertThrows(PaymentErrorException.class, () -> mercadoPagoAdapter.requestPayment(checkoutMessage));
  }

  @Test
  void testHandlePaymentUpdate_successfulApproved() throws Exception {
    // Mock do pagamento existente
    Payment payment = new Payment();
    payment.setExternalId("12345");

    // Mock da resposta do PaymentClient
    com.mercadopago.resources.payment.Payment mockResponse = mock(com.mercadopago.resources.payment.Payment.class);
    when(mockResponse.getStatus()).thenReturn(com.mercadopago.resources.payment.PaymentStatus.APPROVED);
    when(paymentClient.get(12345L)).thenReturn(mockResponse);

    // Execução do método
    Payment result = mercadoPagoAdapter.handlePaymentUpdate(payment);

    // Verificações
    assertNotNull(result);
    assertEquals(PaymentStatus.APPROVED.toString(), result.getStatus());
    assertNotNull(result.getPayedAt());
  }

  @Test
  void testHandlePaymentUpdate_rejectedPayment() throws Exception {
    // Mock do pagamento existente
    Payment payment = new Payment();
    payment.setExternalId("12345");

    // Mock da resposta do PaymentClient
    com.mercadopago.resources.payment.Payment mockResponse = mock(com.mercadopago.resources.payment.Payment.class);
    when(mockResponse.getStatus()).thenReturn(com.mercadopago.resources.payment.PaymentStatus.REJECTED);
    when(paymentClient.get(any())).thenReturn(mockResponse);

    // Execução do método
    Payment result = mercadoPagoAdapter.handlePaymentUpdate(payment);

    // Verificações
    assertNotNull(result);
    assertEquals(PaymentStatus.REFUSED.toString(), result.getStatus());
    assertNull(result.getPayedAt());
  }

  @Test
  void testHandlePaymentUpdate_canceledPayment() throws Exception {
    // Mock do pagamento existente
    Payment payment = new Payment();
    payment.setExternalId("12345");

    // Mock da resposta do PaymentClient
    com.mercadopago.resources.payment.Payment mockResponse = mock(com.mercadopago.resources.payment.Payment.class);
    when(mockResponse.getStatus()).thenReturn(com.mercadopago.resources.payment.PaymentStatus.CANCELLED);
    when(paymentClient.get(any())).thenReturn(mockResponse);

    // Execução do método
    Payment result = mercadoPagoAdapter.handlePaymentUpdate(payment);

    // Verificações
    assertNotNull(result);
    assertEquals(PaymentStatus.CANCELLED.toString(), result.getStatus());
    assertNull(result.getPayedAt());
  }

  @Test
  void testHandlePaymentUpdate_throwsException() throws Exception {
    // Mock do pagamento existente
    Payment payment = new Payment();
    payment.setExternalId("12345");

    // Simula exceção do PaymentClient
    when(paymentClient.get(anyLong())).thenThrow(new MPException("API Error"));

    // Execução e verificação
    assertThrows(PaymentErrorException.class, () -> mercadoPagoAdapter.handlePaymentUpdate(payment));
  }

  @Test
  void testFakeHandlePayment_successful() {
    Payment payment = MockHelper.getCreatedPayment();
    Payment updatedPayment = mercadoPagoAdapter.fakeHandlePayment(payment);

    assertNotNull(updatedPayment.getPayedAt());
    assertEquals(PaymentStatus.APPROVED.toString(), updatedPayment.getStatus());
    assertEquals(payment.getExternalId(), updatedPayment.getExternalId());
    assertEquals(payment.getId(), updatedPayment.getId());
  }
}
