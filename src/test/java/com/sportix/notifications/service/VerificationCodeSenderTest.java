package com.sportix.notifications.service;

import com.sportix.notifications.dto.SendCodeRequest;
import com.sportix.notifications.dto.SendCodeResponse;
import com.sportix.notifications.mail.EmailMessage;
import com.sportix.notifications.mail.MailDeliveryException;
import com.sportix.notifications.mail.MailGateway;
import com.sportix.notifications.model.CodePurpose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VerificationCodeSenderTest {

    private final MailGateway gateway = mock(MailGateway.class);
    private final VerificationCodeSender sender = new VerificationCodeSender(new VerificationEmailTemplate(), gateway);

    @Test
    @DisplayName("arma el correo con el código y devuelve status ok con el id del mensaje")
    void sendsCode() {
        when(gateway.send(any())).thenReturn("<id-1@gmail>");
        SendCodeResponse response = sender.send(new SendCodeRequest("ana@mail.com", "482915", 5, "req-1", CodePurpose.LOGIN_2FA));

        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(gateway).send(sent.capture());
        assertThat(sent.getValue().to()).isEqualTo("ana@mail.com");
        assertThat(sent.getValue().subject()).contains("iniciar sesión");
        assertThat(sent.getValue().textBody()).contains("482915").contains("5 minutos");
        assertThat(sent.getValue().htmlBody()).contains("482915");
        assertThat(response.status()).isEqualTo("ok");
        assertThat(response.messageId()).isEqualTo("<id-1@gmail>");
        assertThat(response.requestId()).isEqualTo("req-1");
    }

    @Test
    @DisplayName("el texto cambia según el propósito (recuperar contraseña)")
    void passwordResetText() {
        when(gateway.send(any())).thenReturn("id");
        sender.send(new SendCodeRequest("ana@mail.com", "123456", 10, null, CodePurpose.PASSWORD_RESET));
        ArgumentCaptor<EmailMessage> sent = ArgumentCaptor.forClass(EmailMessage.class);
        verify(gateway).send(sent.capture());
        assertThat(sent.getValue().subject()).contains("contraseña");
    }

    @Test
    @DisplayName("sin purpose se asume login con 2FA")
    void defaultPurpose() {
        assertThat(new SendCodeRequest("a@b.co", "123456", 5, null, null).purposeOrDefault()).isEqualTo(CodePurpose.LOGIN_2FA);
    }

    @Test
    @DisplayName("si el proveedor falla, el error sube (el controlador responde 502)")
    void deliveryFailure() {
        when(gateway.send(any())).thenThrow(new MailDeliveryException("535 auth", null));
        assertThatThrownBy(() -> sender.send(new SendCodeRequest("a@b.co", "123456", 5, null, null)))
                .isInstanceOf(MailDeliveryException.class);
    }

    @Test
    @DisplayName("modo asíncrono: responde 'queued' sin esperar y el correo sale después")
    void asyncMode() throws Exception {
        java.util.concurrent.CountDownLatch sent = new java.util.concurrent.CountDownLatch(1);
        when(gateway.send(any())).thenAnswer(invocation -> { sent.countDown(); return "id"; });
        var properties = new com.sportix.notifications.config.NotificationProperties("k",
                new com.sportix.notifications.config.NotificationProperties.Mail("smtp", "Sportix", true));
        VerificationCodeSender async = new VerificationCodeSender(new VerificationEmailTemplate(), gateway, properties);

        SendCodeResponse response = async.send(new SendCodeRequest("a@b.co", "123456", 5, "r", null));

        assertThat(response.status()).isEqualTo("ok");
        assertThat(response.messageId()).isEqualTo("queued");
        assertThat(sent.await(2, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
        async.shutdown();
    }

    @Test
    @DisplayName("los logs muestran el correo enmascarado")
    void masksEmail() {
        assertThat(VerificationCodeSender.mask("ana.gomez@mail.com")).isEqualTo("a********@mail.com");
    }
}
