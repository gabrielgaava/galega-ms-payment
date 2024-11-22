package com.galega.payment.infrastructure.adapters.input.web.notifications;

import com.galega.payment.BaseTestEnv;
import com.galega.payment.application.ports.input.UpdatePaymentStatusUseCase;
import com.galega.payment.domain.exception.PaymentErrorException;
import com.galega.payment.infrastructure.adapters.input.queue.SQSHandlerAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MercadoPagoNotificationsController.class)
class MercadoPagoNotificationsControllerTest extends BaseTestEnv {

    private MockMvc mockMvc;

    @Mock
    UpdatePaymentStatusUseCase updatePaymentStatusUseCase;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(new MercadoPagoNotificationsController(updatePaymentStatusUseCase))
            .build();
    }

    @Test
    void instantPaymentNotification_ShouldReturnOk_ForValidPaymentTopic() throws Exception {
        mockMvc.perform(post("/notifications/mercadopago")
                        .param("topic", "payment")
                        .param("id", "12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(updatePaymentStatusUseCase).updatePaymentStatus("12345", Boolean.FALSE);
    }

    @Test
    void instantPaymentNotification_ShouldReturnOk_WhenPaymentErrorExceptionOccurs() throws Exception {
        doThrow(new PaymentErrorException("Payment update failed", "123"))
                .when(updatePaymentStatusUseCase).updatePaymentStatus(anyString(), Mockito.eq(Boolean.FALSE));

        mockMvc.perform(post("/notifications/mercadopago")
                        .param("topic", "payment")
                        .param("id", "12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(updatePaymentStatusUseCase).updatePaymentStatus("12345", Boolean.FALSE);
    }

    @Test
    void instantPaymentNotification_ShouldReturnOk_ForNonPaymentTopic() throws Exception {
        mockMvc.perform(post("/notifications/mercadopago")
                        .param("topic", "subscription")
                        .param("id", "12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Mockito.verifyNoInteractions(updatePaymentStatusUseCase);
    }

    @Test
    void fakePaymentNotification_ShouldReturnOk_ForValidRequest() throws Exception {
        mockMvc.perform(post("/notifications/mercadopago/fake")
                        .param("id", "12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(updatePaymentStatusUseCase).updatePaymentStatus("12345", Boolean.TRUE);
    }

}
