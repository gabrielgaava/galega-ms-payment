package com.galega.payment.application.ports.input;

import com.galega.payment.domain.model.order.Order;
import com.galega.payment.domain.model.payment.Payment;

public interface CreatePaymentUseCase {

  public Payment createPayment(Order order);

}
