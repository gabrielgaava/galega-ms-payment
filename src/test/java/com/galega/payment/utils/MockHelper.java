package com.galega.payment.utils;

import com.galega.payment.domain.model.customer.Customer;
import com.galega.payment.domain.model.order.Order;
import com.galega.payment.domain.model.order.OrderStatus;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.domain.model.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class MockHelper {

  public static Order getCreatedOrder() {
    Order order = new Order();
    order.setId(UUID.randomUUID());
    order.setCustomerId(UUID.fromString("a287c652-75aa-46ff-8ac8-41240b76e0f5"));
    order.setOrderNumber(1234);
    order.setAmount(BigDecimal.valueOf(125.30));
    order.setStatus(OrderStatus.CREATED);
    order.setCreatedAt(LocalDateTime.now().minusMinutes(1));
    order.setPaidAt(null);
    order.setWaitingTimeInSeconds(0);
    return order;
  }

  public static Customer getUnknownCustomer() {
    Customer customer = new Customer();
    customer.setId(UUID.fromString("a287c652-75aa-46ff-8ac8-41240b76e0f5"));
    customer.setName("Usuario não identificado");
    customer.setEmail("unknown@galega.com");
    customer.setCpf("41704858070");
    return customer;
  }

  public static Object getFakeTransactionData() {
    return new Object();
  }

  public static Payment getCreatedPayment() {
    Payment payment = new Payment();
    Order order = getCreatedOrder();

    payment.setId(UUID.randomUUID());
    payment.setExternalId(UUID.randomUUID().toString());
    payment.setOrderId(order.getId());
    payment.setAmount(order.getAmount());
    payment.setPayedAt(LocalDateTime.now());
    payment.setGateway("MercadoPago");
    payment.setStatus(PaymentStatus.PENDING.toString());
    payment.setTransactionData(getFakeTransactionData());
    return payment;
  }

  public static Payment getPaymentPaid() {
    Payment payment = getCreatedPayment();
    payment.setStatus(PaymentStatus.APPROVED.toString());
    payment.setPayedAt(LocalDateTime.now());
    return payment;
  }
}
