package com.galega.payment.domain.exception;

public class PaymentNotFound extends RuntimeException {

  private final String identifierKey;
  private final String identifierValue;

  public PaymentNotFound(String key, String value) {
    super("Payment not found with " + key + " equals to " + value);
    this.identifierKey = key;
    this.identifierValue = value;
  }

  public String getIdentifierKey() {
    return identifierKey;
  }

  public String getIdentifierValue() {
    return identifierValue;
  }
}
