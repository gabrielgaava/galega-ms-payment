package com.galega.payment.infrastructure.adapters.input.web.notifications;

import com.galega.payment.application.dto.PaymentWebhookDTO;
import com.galega.payment.application.ports.output.PaymentGatewayPort;
import com.galega.payment.domain.exception.PaymentErrorException;
import com.galega.payment.domain.model.payment.Payment;
import com.galega.payment.domain.service.PaymentService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Webhook Mercado Pago Controller")
@RestController
@RequestMapping("/notifications/mercadopago")
public class MercadoPagoNotificationsController {

  private PaymentService paymentService;

  public MercadoPagoNotificationsController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PostMapping
  public ResponseEntity<?> instantPaymentNotification(@RequestParam String topic, @RequestParam String id)  {

    if(topic.equals("payment")){
      try {
        paymentService.updatePaymentStatus(id, Boolean.FALSE);
      }
      catch (PaymentErrorException e ) {
        // Mercado Pago Service unavailable, or database error, should do a internal retry
        return ResponseEntity.ok().build();
      }

    }
    // Must return 200 for mercadopago request
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Simulate the webhook integration, when 'approve' is true, simulates the success of the payment",
      parameters = {
          @Parameter(name = "approve", schema = @Schema(implementation = Boolean.class)),
          @Parameter(name = "id", schema = @Schema(implementation = String.class)),
      })
  @PostMapping("/fake")
  public ResponseEntity<?> fakePaymentNotification(@RequestParam String id) throws PaymentErrorException {

    paymentService.updatePaymentStatus(id, Boolean.TRUE);
    return ResponseEntity.ok("Ok");

  }

}
