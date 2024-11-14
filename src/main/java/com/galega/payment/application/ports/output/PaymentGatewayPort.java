package com.galega.payment.application.ports.output;

import com.galega.payment.domain.exception.PaymentErrorException;
import com.galega.payment.domain.model.payment.CheckoutMessage;
import com.galega.payment.domain.model.payment.Payment;

public interface PaymentGatewayPort {

  Payment requestPayment(CheckoutMessage checkoutMessage) throws PaymentErrorException;

  Payment handlePaymentUpdate(Payment payment) throws PaymentErrorException;

  Payment fakeHandlePayment(Payment payment);

}
