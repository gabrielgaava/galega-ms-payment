package com.galega.payment.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentWebhookDTO {

  private LocalDateTime payedAt;
  private BigDecimal amount;
  private String gateway;
  private String externalId;
  private String status;
  private UUID paymentId;
  private UUID orderId;

}