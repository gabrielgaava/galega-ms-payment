package com.galega.payment.domain.service;

import com.galega.payment.application.ports.input.CreatePaymentUseCase;
import com.galega.payment.application.ports.input.GetPaymentUseCase;
import com.galega.payment.application.ports.input.UpdatePaymentStatusUseCase;
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

import java.util.List;

public class PaymentService implements CreatePaymentUseCase, GetPaymentUseCase, UpdatePaymentStatusUseCase {

  private final PaymentRepositoryPort paymentRepositoryPort;
  private final CustomerPort customerPort;
  private final PaymentGatewayPort paymentGatewayPort;
  private final NotifyPaymentPort notifyPaymentPort;

  public PaymentService(PaymentRepositoryPort paymentRepositoryPort, CustomerPort customerPort, PaymentGatewayPort paymentGatewayPort, NotifyPaymentPort notifyPaymentPort) {
    this.paymentRepositoryPort = paymentRepositoryPort;
    this.customerPort = customerPort;
    this.paymentGatewayPort = paymentGatewayPort;
    this.notifyPaymentPort = notifyPaymentPort;
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
      Payment updatedPayment = paymentGatewayPort.fakeHandlePayment(storedPayment);
      notifyPaymentPort.notifyPaymentApproved(updatedPayment);
      return paymentRepositoryPort.createOrUpdate(updatedPayment);
    }

    Payment updatedPayment = paymentGatewayPort.handlePaymentUpdate(storedPayment);
    this.handlePaymentNotification(updatedPayment);
    return paymentRepositoryPort.createOrUpdate(updatedPayment);
  }

  @Override
  public Payment getByPaymentId(String id) {
    return paymentRepositoryPort.findBy("id", id);
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

  private void handlePaymentNotification(Payment payment) {
    String status = payment.getStatus();

    if(status.equals(PaymentStatus.APPROVED.toString()))
      notifyPaymentPort.notifyPaymentApproved(payment);

    if (status.equals(PaymentStatus.REFUSED.toString()))
      notifyPaymentPort.notifyPaymentRefused(payment);

    if (status.equals(PaymentStatus.CANCELLED.toString()))
      notifyPaymentPort.notifyPaymentCanceled(payment);

  }

}
