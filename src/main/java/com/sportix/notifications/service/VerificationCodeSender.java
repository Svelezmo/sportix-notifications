package com.sportix.notifications.service;

import com.sportix.notifications.dto.SendCodeRequest;
import com.sportix.notifications.dto.SendCodeResponse;
import com.sportix.notifications.mail.EmailMessage;
import com.sportix.notifications.mail.MailGateway;
import com.sportix.notifications.config.NotificationProperties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Caso de uso del servicio: "entregar un código de verificación".
 * 1. Arma el correo (VerificationEmailTemplate).
 * 2. Lo envía por el canal configurado (MailGateway: SMTP o consola).
 * 3. Devuelve el id del mensaje para que Sportixcore lo registre.
 *
 * Con MAIL_ASYNC=true responde de inmediato ("queued") y el envío sigue en
 * segundo plano: el login no espera a Gmail, pero si Gmail falla solo queda
 * en el log de este servicio (Sportixcore ya no se entera).
 *
 * Nunca escribe el código en el log (salvo en modo consola, que es justamente
 * para verlo en desarrollo): en producción el log no debe contener secretos.
 */
@Service
public class VerificationCodeSender {

    private static final Logger log = LoggerFactory.getLogger(VerificationCodeSender.class);

    private final VerificationEmailTemplate template;
    private final MailGateway mailGateway;
    /** null = modo síncrono */
    private final ExecutorService background;

    @Autowired
    public VerificationCodeSender(VerificationEmailTemplate template, MailGateway mailGateway,
                                  NotificationProperties properties) {
        this.template = template;
        this.mailGateway = mailGateway;
        boolean async = properties.mail() != null && properties.mail().async();
        // 2 hilos bastan: los correos de verificación son pocos y cortos
        this.background = async ? Executors.newFixedThreadPool(2) : null;
        log.info("Envío de correos: {}", async ? "ASÍNCRONO (responde sin esperar a Gmail)" : "síncrono");
    }

    /** Modo síncrono (pruebas) */
    public VerificationCodeSender(VerificationEmailTemplate template, MailGateway mailGateway) {
        this.template = template;
        this.mailGateway = mailGateway;
        this.background = null;
    }

    @PreDestroy
    void shutdown() {
        if (background != null) {
            background.shutdown();
        }
    }

    public SendCodeResponse send(SendCodeRequest request) {
        EmailMessage message = template.build(request.to(), request.code(), request.ttlMinutes(), request.purposeOrDefault());
        if (background != null) {
            background.submit(() -> {
                try {
                    deliver(request, message);
                } catch (RuntimeException exception) {
                    log.error("No se pudo entregar el código {} a {} (request_id={}): {}",
                            request.purposeOrDefault().getCode(), mask(request.to()), request.requestId(),
                            exception.getMessage());
                }
            });
            return SendCodeResponse.ok("queued", mailGateway.isDryRun(), request.requestId());
        }
        return SendCodeResponse.ok(deliver(request, message), mailGateway.isDryRun(), request.requestId());
    }

    private String deliver(SendCodeRequest request, EmailMessage message) {
        long start = System.nanoTime();
        String messageId = mailGateway.send(message);
        long millis = (System.nanoTime() - start) / 1_000_000;

        // Gmail suele tardar 1–5 s por envío (TLS + autenticación); el tiempo
        // ayuda a ajustar NOTIFICATION_TIMEOUT_SECONDS en Sportixcore.
        log.info("Código {} entregado a {} en {} ms (request_id={}, message_id={})",
                request.purposeOrDefault().getCode(), mask(request.to()), millis, request.requestId(), messageId);
        return messageId;
    }

    /** ana.gomez@mail.com -> a********@mail.com (los logs no exponen correos completos) */
    static String mask(String email) {
        int at = email.indexOf('@');
        return at <= 1 ? "***" + email.substring(Math.max(at, 0)) : email.charAt(0) + "*".repeat(at - 1) + email.substring(at);
    }
}
