package com.galega.payment.domain.service;

import com.galega.payment.application.ports.input.CreatePaymentUseCase;
import com.galega.payment.application.ports.input.GetPaymentUseCase;
import com.galega.payment.application.ports.input.UpdatePaymentStatusUseCase;
import com.galega.payment.application.ports.output.CustomerPort;
import com.galega.payment.application.ports.output.PaymentGatewayPort;
import com.galega.payment.application.ports.output.PaymentRepositoryPort;
import com.galega.payment.domain.exception.PaymentErrorException;
import com.galega.payment.domain.model.customer.Customer;
import com.galega.payment.domain.model.order.Order;
import com.galega.payment.domain.model.payment.CheckoutMessage;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.domain.model.payment.PaymentStatus;
import com.galega.payment.infrastructure.adapters.output.repository.dynamodb.PaymentDynamoAdapter;
import com.galega.payment.infrastructure.adapters.output.rest.CustomerApiAdapter;
import com.galega.payment.infrastructure.adapters.output.rest.MercadoPagoAdapter;

import java.math.BigDecimal;
import java.util.List;

public class PaymentService implements CreatePaymentUseCase, GetPaymentUseCase, UpdatePaymentStatusUseCase {

  private final PaymentRepositoryPort paymentRepositoryPort;
  private final CustomerPort customerPort;
  private final PaymentGatewayPort paymentGatewayPort;

  public PaymentService(PaymentRepositoryPort paymentRepositoryPort, CustomerPort customerPort, PaymentGatewayPort paymentGatewayPort) {
    this.paymentRepositoryPort = paymentRepositoryPort;
    this.customerPort = customerPort;
    this.paymentGatewayPort = paymentGatewayPort;
  }

  @Override
  public Payment createPayment(Order order) {

    try {
      Customer customer = customerPort.getCustomerById(order.getCustomerId().toString());
      CheckoutMessage message = this.createCheckoutMessage(order, customer);
      Payment payment = paymentGatewayPort.requestPayment(message);
      return paymentRepositoryPort.createOrUpdate(payment);
    }

    catch (PaymentErrorException exception) {
      return null;
    }

  }

  @Override
  public Payment updatePaymentStatus(String externalId, Boolean isFake) throws PaymentErrorException {
    Payment storedPayment = paymentRepositoryPort.findBy("externalId", externalId);

    if(storedPayment == null) {
      throw new PaymentErrorException("not found", "MercadoPago");
    }

    if(isFake) {
      return paymentGatewayPort.fakeHandlePayment(storedPayment);
    }

    return paymentGatewayPort.handlePaymentUpdate(storedPayment);
  }

  @Override
  public Payment getByPaymentId(String id) {
    return null;
  }

  @Override
  public Payment getByPaymentExternalId(String id) {
    return paymentRepositoryPort.findBy("externalId", id);
  }

  @Override
  public Payment getByPaymentOrderId(String id) {
    return paymentRepositoryPort.findBy("orderId", id);
  }

  @Override
  public List<Payment> getAllPayments() {
    return paymentRepositoryPort.getAll();
  }

  private CheckoutMessage createCheckoutMessage(Order order, Customer customer) {

      CheckoutMessage checkoutMessage = new CheckoutMessage();
      checkoutMessage.setOrderId(order.getId().toString());
      checkoutMessage.setOrderAmount(order.getAmount().toString());
      checkoutMessage.setPaymentGateway("MercadoPago");

      if(customer != null) {
        checkoutMessage.setCustomerDocument(customer.getCpf());
        checkoutMessage.setCustomerEmail(customer.getEmail());
        checkoutMessage.setCustomerName(customer.getName());
      }

      return checkoutMessage;
  }

}
