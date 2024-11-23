package com.galega.payment.application.ports.output;

import com.galega.payment.domain.model.payment.Payment;

public interface NotifyPaymentPort {

  void notifyPaymentApproved(Payment payment);

  void notifyPaymentRefused(Payment payment);

  void notifyPaymentCanceled(Payment payment);

}
