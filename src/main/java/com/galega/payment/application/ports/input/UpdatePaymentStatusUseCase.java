package com.galega.payment.application.ports.input;

import com.galega.payment.domain.exception.PaymentErrorException;
import com.galega.payment.domain.model.payment.Payment;

public interface UpdatePaymentStatusUseCase {

  Payment updatePaymentStatus(String externalId, Boolean isFake) throws PaymentErrorException;

}
