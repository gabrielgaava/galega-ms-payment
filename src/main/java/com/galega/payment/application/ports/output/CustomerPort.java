package com.galega.payment.application.ports.output;

import com.galega.payment.domain.model.customer.Customer;

public interface CustomerPort {

  Customer getCustomerByCPF(String cpf);

  Customer getCustomerById(String id);

}
