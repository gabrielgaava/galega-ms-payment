package com.galega.payment.infrastructure.adapters.input.web.api;

import com.galega.payment.domain.model.Payment;
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

  private final PaymentDynamoAdapter createPaymentGetaway;

  public PaymentController(DynamoDbClient dynamoDbClient) {
    this.createPaymentGetaway = new PaymentDynamoAdapter(dynamoDbClient);
  }

  @PostMapping
  public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
    Payment createdPayment = createPaymentGetaway.createOrUpdate(payment);
    return ResponseEntity.ok(createdPayment);
  }

  @GetMapping
  public ResponseEntity<List<Payment>> getAllPayments() {
    List<Payment> payment = createPaymentGetaway.getAll();
    return ResponseEntity.ok(payment);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Payment> getPayment(@PathVariable String id) {
    Payment payment = createPaymentGetaway.findBy("id", id);
    return ResponseEntity.ok(payment);
  }

}
