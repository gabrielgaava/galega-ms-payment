package com.galega.payment.infrastructure.adapters.input.web.api;

import com.galega.payment.application.ports.input.CreatePaymentUseCase;
import com.galega.payment.application.ports.input.GetPaymentUseCase;
import com.galega.payment.domain.model.order.Order;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.domain.service.PaymentService;
import com.galega.payment.infrastructure.adapters.output.repository.dynamodb.PaymentDynamoAdapter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

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

  @PostMapping
  public ResponseEntity<Payment> createPayment(@RequestBody Order order) {
    Payment createdPayment = createPaymentUseCase.createPayment(order);
    return ResponseEntity.ok(createdPayment);
  }

  @GetMapping
  public ResponseEntity<List<Payment>> getAllPayments() {
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
