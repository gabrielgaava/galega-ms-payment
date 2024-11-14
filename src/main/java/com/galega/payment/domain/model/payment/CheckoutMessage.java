package com.galega.payment.domain.model.payment;

public class CheckoutMessage {

  String orderId;
  String orderAmount;
  String customerName;
  String customerEmail;
  String customerDocument;
  String paymentGateway;

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public String getOrderAmount() {
    return orderAmount;
  }

  public void setOrderAmount(String orderAmount) {
    this.orderAmount = orderAmount;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }

  public String getCustomerDocument() {
    return customerDocument;
  }

  public void setCustomerDocument(String customerDocument) {
    this.customerDocument = customerDocument;
  }

  public String getPaymentGateway() {
    return paymentGateway;
  }

  public void setPaymentGateway(String paymentGateway) {
    this.paymentGateway = paymentGateway;
  }
}
