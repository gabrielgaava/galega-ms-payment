package com.galega.payment.application.ports.input;

import com.galega.payment.domain.exception.PaymentNotFound;
import com.galega.payment.domain.model.payment.Payment;

public interface GetPaymentUseCase {

  public Payment getByPaymentId(String id) throws PaymentNotFound;

  public Payment getByPaymentExternalId(String id) throws PaymentNotFound;

  public Payment getByPaymentOrderId(String id) throws PaymentNotFound;

  public Payment getAllPayments();

}
