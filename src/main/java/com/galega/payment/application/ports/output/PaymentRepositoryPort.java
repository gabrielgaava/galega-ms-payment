package com.galega.payment.application.ports.output;

import com.galega.payment.domain.model.payment.Payment;

import java.util.List;

public interface PaymentRepositoryPort {

  Payment createOrUpdate(Payment payment);

  Payment findBy(String key, String value);

  List<Payment> getAll();

}
