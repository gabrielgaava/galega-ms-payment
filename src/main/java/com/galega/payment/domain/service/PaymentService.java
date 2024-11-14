package com.galega.payment.domain.service;

import com.galega.payment.application.ports.input.CreatePaymentUseCase;
import com.galega.payment.application.ports.input.GetPaymentUseCase;
import com.galega.payment.application.ports.input.UpdatePaymentStatusUseCase;
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

public class PaymentService implements CreatePaymentUseCase, GetPaymentUseCase, UpdatePaymentStatusUseCase {

  private final PaymentDynamoAdapter paymentDynamoAdapter;
  private final CustomerApiAdapter customerApiAdapter;
  private final MercadoPagoAdapter mercadoPagoAdapter;

  public PaymentService(PaymentDynamoAdapter paymentDynamoAdapter, CustomerApiAdapter customerApiAdapter, MercadoPagoAdapter mercadoPagoAdapter) {
    this.paymentDynamoAdapter = paymentDynamoAdapter;
    this.customerApiAdapter = customerApiAdapter;
    this.mercadoPagoAdapter = mercadoPagoAdapter;
  }

  @Override
  public Payment createPayment(Order order) {

    try {
      Customer customer = customerApiAdapter.getCustomerById(order.getCustomerId().toString());
      CheckoutMessage message = this.createCheckoutMessage(order, customer);
      Payment payment = mercadoPagoAdapter.requestPayment(message);
      return paymentDynamoAdapter.createOrUpdate(payment);
    }

    catch (PaymentErrorException exception) {
      return null;
    }

  }

  @Override
  public Payment updatePaymentStatus(String externalId, Boolean isFake) throws PaymentErrorException {
    Payment storedPayment = paymentDynamoAdapter.findBy("externalId", externalId);

    if(isFake) {
      return mercadoPagoAdapter.fakeHandlePayment(storedPayment);
    }

    return mercadoPagoAdapter.handlePaymentUpdate(storedPayment);
  }

  @Override
  public Payment getByPaymentId(String id) {
    return null;
  }

  @Override
  public Payment getByPaymentExternalId(String id) {
    return null;
  }

  @Override
  public Payment getByPaymentOrderId(String id) {
    return null;
  }

  @Override
  public Payment getAllPayments() {
    return null;
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
