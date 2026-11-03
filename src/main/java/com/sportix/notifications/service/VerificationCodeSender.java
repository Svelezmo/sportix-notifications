package com.sportix.notifications.service;

import com.sportix.notifications.dto.SendCodeRequest;
import com.sportix.notifications.dto.SendCodeResponse;
import com.sportix.notifications.mail.EmailMessage;
import com.sportix.notifications.mail.MailGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Caso de uso del servicio: "entregar un código de verificación".
 * 1. Arma el correo (VerificationEmailTemplate).
 * 2. Lo envía por el canal configurado (MailGateway: SMTP o consola).
 * 3. Devuelve el id del mensaje para que Sportixcore lo registre.
 *
 * Nunca escribe el código en el log (salvo en modo consola, que es justamente
 * para verlo en desarrollo): en producción el log no debe contener secretos.
 */
@Service
public class VerificationCodeSender {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeSender.class);

    private final VerificationEmailTemplate template;
    private final MailGateway mailGateway;

    public VerificationCodeSender(VerificationEmailTemplate template, MailGateway mailGateway) {
        this.template = template;
        this.mailGateway = mailGateway;
    }

    public SendCodeResponse send(SendCodeRequest request) {
        EmailMessage message = template.build(request.to(), request.code(), request.ttlMinutes(), request.purposeOrDefault());
        long start = System.nanoTime();
        String messageId = mailGateway.send(message);
        long millis = (System.nanoTime() - start) / 1_000_000;

        // Gmail suele tardar 1–5 s por envío (TLS + autenticación); el tiempo
        // ayuda a ajustar NOTIFICATION_TIMEOUT_SECONDS en Sportixcore.
        log.info("Código {} entregado a {} en {} ms (request_id={}, message_id={})",
                request.purposeOrDefault().getCode(), mask(request.to()), millis, request.requestId(), messageId);

        return SendCodeResponse.ok(messageId, mailGateway.isDryRun(), request.requestId());
    }

    /** ana.gomez@mail.com -> a********@mail.com (los logs no exponen correos completos) */
    static String mask(String email) {
        int at = email.indexOf('@');
        return at <= 1 ? "***" + email.substring(Math.max(at, 0)) : email.charAt(0) + "*".repeat(at - 1) + email.substring(at);
    }
}
