package com.galega.payment.domain.service;

import com.galega.payment.application.ports.input.CreatePaymentUseCase;
import com.galega.payment.application.ports.input.GetPaymentUseCase;
import com.galega.payment.domain.model.Payment;
import com.galega.payment.infrastructure.adapters.output.repository.dynamodb.PaymentDynamoAdapter;

public class PaymentService implements CreatePaymentUseCase, GetPaymentUseCase {

  private final PaymentDynamoAdapter createPaymentGetaway;

  public PaymentService(PaymentDynamoAdapter createPaymentGetaway) {
    this.createPaymentGetaway = createPaymentGetaway;
  }

  @Override
  public Payment createPayment(Payment payment) {
     return createPaymentGetaway.createOrUpdate(payment);
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

}
