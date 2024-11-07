package com.galega.payment.application.ports.output;

import com.galega.payment.domain.model.Payment;

import java.util.List;

public interface PaymentRepositoryPort {

  public Payment createOrUpdate(Payment payment);

  public Payment findBy(String key, String value);

  public List<Payment> getAll();

}
