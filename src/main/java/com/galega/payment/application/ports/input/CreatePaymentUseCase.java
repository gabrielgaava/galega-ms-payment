package com.galega.payment.application.ports.input;

import com.galega.payment.domain.model.Payment;

public interface CreatePaymentUseCase {

  public Payment createPayment(Payment payment);

}
