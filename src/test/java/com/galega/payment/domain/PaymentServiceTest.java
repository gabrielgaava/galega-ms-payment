package com.galega.payment.domain;

import com.galega.payment.BaseTestEnv;
import com.galega.payment.application.ports.output.CustomerPort;
import com.galega.payment.application.ports.output.NotifyPaymentPort;
import com.galega.payment.application.ports.output.PaymentGatewayPort;
import com.galega.payment.application.ports.output.PaymentRepositoryPort;
import com.galega.payment.domain.exception.PaymentErrorException;
import com.galega.payment.domain.model.customer.Customer;
import com.galega.payment.domain.model.order.Order;
import com.galega.payment.domain.model.payment.CheckoutMessage;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.domain.model.payment.PaymentStatus;
import com.galega.payment.domain.service.PaymentService;
import com.galega.payment.infrastructure.adapters.input.queue.SQSHandlerAdapter;
import com.galega.payment.utils.MockHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest extends BaseTestEnv {

  private PaymentService paymentService;
  private PaymentRepositoryPort paymentRepositoryPort;
  private CustomerPort customerPort;
  private PaymentGatewayPort paymentGatewayPort;
  private NotifyPaymentPort notifyPaymentPort;

  @BeforeEach
  public void setUp() {
    paymentRepositoryPort = mock(PaymentRepositoryPort.class);
    customerPort = mock(CustomerPort.class);
    paymentGatewayPort = mock(PaymentGatewayPort.class);
    notifyPaymentPort = mock(NotifyPaymentPort.class);
    paymentService = new PaymentService(paymentRepositoryPort, customerPort, paymentGatewayPort, notifyPaymentPort);
  }

  @Test
  void createPayment_successful() throws PaymentErrorException {
    // Arrange
    Order order = MockHelper.getCreatedOrder();
    Customer customer = MockHelper.getUnknownCustomer();
    Payment payment = MockHelper.getCreatedPayment();

    when(customerPort.getCustomerById(order.getCustomerId().toString())).thenReturn(customer);
    when(paymentGatewayPort.requestPayment(any(CheckoutMessage.class))).thenReturn(payment);
    when(paymentRepositoryPort.createOrUpdate(payment)).thenReturn(payment);

    // Act
    Payment result = paymentService.createPayment(order);

    // Assert
    assertNotNull(result);
    assertThat(result).usingRecursiveComparison().isEqualTo(payment);
    verify(paymentRepositoryPort, times(1)).createOrUpdate(payment);
    verify(paymentGatewayPort, times(1)).requestPayment(any(CheckoutMessage.class));
    verify(customerPort, times(1)).getCustomerById(order.getCustomerId().toString());
  }

  @Test
  void createPayment_successfulWithNullCustomer() throws PaymentErrorException {
    // Arrange
    Order order = MockHelper.getCreatedOrder();
    Payment payment = MockHelper.getCreatedPayment();

    when(customerPort.getCustomerById(order.getCustomerId().toString())).thenReturn(null);
    when(paymentGatewayPort.requestPayment(any(CheckoutMessage.class))).thenReturn(payment);
    when(paymentRepositoryPort.createOrUpdate(payment)).thenReturn(payment);

    // Act
    Payment result = paymentService.createPayment(order);

    // Assert
    assertNotNull(result);
    assertThat(result).usingRecursiveComparison().isEqualTo(payment);
    verify(paymentRepositoryPort, times(1)).createOrUpdate(payment);
    verify(paymentGatewayPort, times(1)).requestPayment(any(CheckoutMessage.class));
    verify(customerPort, times(1)).getCustomerById(order.getCustomerId().toString());
  }

  @Test
  void createPayment_error() throws PaymentErrorException {
    // Arrange
    Order order = MockHelper.getCreatedOrder();
    Customer customer = MockHelper.getUnknownCustomer();
    Payment payment = MockHelper.getCreatedPayment();

    when(customerPort.getCustomerById(order.getCustomerId().toString())).thenReturn(customer);
    when(paymentGatewayPort.requestPayment(any(CheckoutMessage.class))).thenReturn(payment);
    when(paymentRepositoryPort.createOrUpdate(payment)).thenThrow(
        new PaymentErrorException(payment.getOrderId().toString(), "Erro")
    );

    // Act
    Payment result = paymentService.createPayment(order);

    // Assert
    assertNull(result);
    verify(paymentRepositoryPort, times(1)).createOrUpdate(payment);
    verify(paymentGatewayPort, times(1)).requestPayment(any(CheckoutMessage.class));
    verify(customerPort, times(1)).getCustomerById(order.getCustomerId().toString());
  }

  @Disabled
  void createPayment_customerNotFound() throws PaymentErrorException {
    // Arrange
    Order order = MockHelper.getCreatedOrder();
    when(customerPort.getCustomerById(order.getCustomerId().toString())).thenReturn(null);

    // Act
    Payment result = paymentService.createPayment(order);

    // Assert
    assertNull(result);
    verify(paymentRepositoryPort, never()).createOrUpdate(any());
  }

  @Test
  void updatePaymentStatus_successfulFakeUpdate() throws PaymentErrorException {
    // Arrange
    Payment payment = MockHelper.getCreatedPayment();
    Payment updatedPayment = MockHelper.getPaymentPaid();

    when(paymentRepositoryPort.findBy("externalId", payment.getExternalId())).thenReturn(payment);
    when(paymentGatewayPort.fakeHandlePayment(payment)).thenReturn(updatedPayment);
    when(paymentRepositoryPort.createOrUpdate(any(Payment.class))).thenReturn(updatedPayment);

    // Act
    Payment result = paymentService.updatePaymentStatus(payment.getExternalId(), true);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getPayedAt());
    assertThat(result).usingRecursiveComparison().isEqualTo(updatedPayment);
    verify(paymentGatewayPort).fakeHandlePayment(payment);
  }

  @Test
  void updatePaymentStatus_APPROVED_successfulRealUpdate() throws PaymentErrorException {
    // Arrange
    Payment payment = MockHelper.getCreatedPayment();
    Payment updatedPayment = MockHelper.getPaymentPaid();
    updatedPayment.setStatus(PaymentStatus.APPROVED.toString());

    when(paymentRepositoryPort.findBy("externalId", payment.getExternalId())).thenReturn(payment);
    when(paymentGatewayPort.handlePaymentUpdate(payment)).thenReturn(updatedPayment);
    when(paymentRepositoryPort.createOrUpdate(any(Payment.class))).thenReturn(updatedPayment);

    // Act
    Payment result = paymentService.updatePaymentStatus(payment.getExternalId(), false);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getPayedAt());
    assertThat(result).usingRecursiveComparison().isEqualTo(updatedPayment);
    verify(paymentGatewayPort).handlePaymentUpdate(payment);
  }

  @Test
  void updatePaymentStatus_REFUSED_successfulRealUpdate() throws PaymentErrorException {
    // Arrange
    Payment payment = MockHelper.getCreatedPayment();
    Payment updatedPayment = MockHelper.getPaymentPaid();
    updatedPayment.setStatus(PaymentStatus.REFUSED.toString());

    when(paymentRepositoryPort.findBy("externalId", payment.getExternalId())).thenReturn(payment);
    when(paymentGatewayPort.handlePaymentUpdate(payment)).thenReturn(updatedPayment);
    when(paymentRepositoryPort.createOrUpdate(any(Payment.class))).thenReturn(updatedPayment);

    // Act
    Payment result = paymentService.updatePaymentStatus(payment.getExternalId(), false);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getPayedAt());
    assertThat(result).usingRecursiveComparison().isEqualTo(updatedPayment);
    verify(paymentGatewayPort).handlePaymentUpdate(payment);
  }

  @Test
  void updatePaymentStatus_CANCELLED_successfulRealUpdate() throws PaymentErrorException {
    // Arrange
    Payment payment = MockHelper.getCreatedPayment();
    Payment updatedPayment = MockHelper.getPaymentPaid();
    updatedPayment.setStatus(PaymentStatus.CANCELLED.toString());

    when(paymentRepositoryPort.findBy("externalId", payment.getExternalId())).thenReturn(payment);
    when(paymentGatewayPort.handlePaymentUpdate(payment)).thenReturn(updatedPayment);
    when(paymentRepositoryPort.createOrUpdate(any(Payment.class))).thenReturn(updatedPayment);

    // Act
    Payment result = paymentService.updatePaymentStatus(payment.getExternalId(), false);

    // Assert
    assertNotNull(result);
    assertNotNull(result.getPayedAt());
    assertThat(result).usingRecursiveComparison().isEqualTo(updatedPayment);
    verify(paymentGatewayPort).handlePaymentUpdate(payment);
  }

  @Test
  void updatePaymentStatus_paymentNotFound() {
    // Arrange
    when(paymentRepositoryPort.findBy("externalId", "1")).thenReturn(null);

    // Assert
    assertThrows(PaymentErrorException.class, () -> paymentService.updatePaymentStatus("1", false));
  }

  @Test
  void getByPaymentExternalId_successful() {
    // Arrange
    Payment payment = MockHelper.getCreatedPayment();
    when(paymentRepositoryPort.findBy("externalId", payment.getExternalId())).thenReturn(payment);

    // Act
    Payment result = paymentService.getByPaymentExternalId(payment.getExternalId());

    // Assert
    assertNotNull(result);
    assertEquals(payment.getExternalId(), result.getExternalId());
    verify(paymentRepositoryPort).findBy("externalId", payment.getExternalId());
  }

  @Test
  void getByOrderId_successful() {
    Payment payment = paymentService.getByPaymentOrderId("123");
    assertNull(payment);
  }

  @Test
  void getByPaymentId_successful() {
    Payment payment = paymentService.getByPaymentId("123");
    assertNull(payment);
  }

  @Test
  void getAllPayments_successful() {
    // Arrange
    Payment payment1 = MockHelper.getCreatedPayment();
    Payment payment2 = MockHelper.getCreatedPayment();
    when(paymentRepositoryPort.getAll()).thenReturn(List.of(payment1, payment2));

    // Act
    List<Payment> result = paymentService.getAllPayments();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(paymentRepositoryPort).getAll();
  }
}
