package com.galega.payment.domain.model.payment;

public enum PaymentStatus {

  PENDING ("PENDING"),
  REFUSED ("REFUSED"),
  APPROVED ("APPROVED"),
  CANCELLED ("CANCELLED");

  private final String status;

  PaymentStatus(String status){
    this.status = status;
  }

  public static PaymentStatus fromString(String status){
    if(status == null) return null;

    for(PaymentStatus paymentStatus : values()){
      if(paymentStatus.status.equals(status.toUpperCase()))
        return paymentStatus;
    }

    return null;
  }

  public String toString() {
    return this.status;
  }

}
