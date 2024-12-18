package com.galega.payment.infrastructure.adapters.input.web.api;

import com.galega.payment.application.ports.input.CreatePaymentUseCase;
import com.galega.payment.application.ports.input.GetPaymentUseCase;
import com.galega.payment.domain.model.order.Order;
import com.galega.payment.domain.model.payment.Payment;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment Controller")
public class PaymentController {

  private final CreatePaymentUseCase createPaymentUseCase;
  private final GetPaymentUseCase getPaymentUseCase;

  public PaymentController(CreatePaymentUseCase createPaymentUseCase, GetPaymentUseCase getPaymentUseCase) {
    this.createPaymentUseCase = createPaymentUseCase;
    this.getPaymentUseCase = getPaymentUseCase;
  }

  @GetMapping
  public ResponseEntity<List<Payment>> getAllPayments(
      @RequestParam(value = "filterBy", required = false) String filterField,
      @RequestParam(value = "filterValue", required = false) String filterValue
  ){
    if(filterField != null && filterValue != null){
      Payment payment = getPaymentUseCase.findByFilter(filterField, filterValue);

      if(payment == null) {
        return ResponseEntity.ok(Collections.emptyList());
      }

      return ResponseEntity.ok(Collections.singletonList(payment));
    }

    List<Payment> payment = getPaymentUseCase.getAllPayments();
    return ResponseEntity.ok(payment);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Payment> getPayment(
      @PathVariable String id,
      @RequestParam(value = "isExternal", required = false) Boolean isExternal
  ) {

    Payment payment;

    if(isExternal == null || isExternal.equals(Boolean.FALSE)) {
      payment = getPaymentUseCase.getByPaymentId(id);
    }

    else {
      payment = getPaymentUseCase.getByPaymentExternalId(id);
    }

    return ResponseEntity.ok(payment);
  }

  @PostMapping("/fake-checkout")
  public ResponseEntity<Payment> createPaymentFake(@RequestBody Order order) {
    Payment payment = createPaymentUseCase.createPayment(order);
    return ResponseEntity.ok(payment);
  }

}
