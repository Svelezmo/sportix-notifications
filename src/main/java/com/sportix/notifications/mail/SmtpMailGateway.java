package com.sportix.notifications.mail;

import com.sportix.notifications.config.NotificationProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Envío real por SMTP (Gmail: smtp.gmail.com:587 + STARTTLS + contraseña de
 * aplicación). Se activa con notifications.mail.mode=smtp.
 *
 * Se eligió SMTP en vez de Gmail API porque solo necesita una contraseña de
 * aplicación: sin proyecto en Google Cloud, sin pantalla de consentimiento y
 * sin token que se vence.
 */
@Component
@ConditionalOnProperty(name = "notifications.mail.mode", havingValue = "smtp")
public class SmtpMailGateway implements MailGateway {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailGateway.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String fromName;

    public SmtpMailGateway(JavaMailSender mailSender,
                           NotificationProperties properties,
                           MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.fromAddress = mailProperties.getUsername();
        this.fromName = properties.mail().fromName();
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException("MAIL_MODE=smtp necesita GMAIL_USERNAME y GMAIL_APP_PASSWORD en el .env");
        }
        log.info("Correo: SMTP {}:{} como {}", mailProperties.getHost(), mailProperties.getPort(), fromAddress);
    }

    @Override
    public String send(EmailMessage message) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress, fromName);
            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.textBody(), message.htmlBody());
            mailSender.send(mime);
            return mime.getMessageID();
        } catch (MailException | MessagingException | UnsupportedEncodingException exception) {
            throw new MailDeliveryException("El servidor de correo no aceptó el mensaje: " + exception.getMessage(), exception);
        }
    }

    @Override
    public boolean isDryRun() {
        return false;
    }
}
