package com.galega.payment.domain.model.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Payment {
  private LocalDateTime payedAt;
  private BigDecimal amount;
  private String gateway;
  private String externalId;
  private String status;
  private UUID id;
  private UUID orderId;
  private Object transactionData;

  public Payment() {}

  public LocalDateTime getPayedAt() {
    return payedAt;
  }

  public void setPayedAt(LocalDateTime payedAt) {
    this.payedAt = payedAt;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getGateway() {
    return gateway;
  }

  public void setGateway(String gateway) {
    this.gateway = gateway;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getOrderId() {
    return orderId;
  }

  public void setOrderId(UUID orderId) {
    this.orderId = orderId;
  }

  public Object getTransactionData() {
    return transactionData;
  }

  public void setTransactionData(Object transactionData) {
    this.transactionData = transactionData;
  }
}
