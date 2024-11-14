package com.galega.payment.domain.exception;

public class PaymentErrorException extends Exception {

  public PaymentErrorException(String orderId, String gateway) {
    super("Error in the attempt to pay with " + gateway + " for order " + orderId);
  }

}
