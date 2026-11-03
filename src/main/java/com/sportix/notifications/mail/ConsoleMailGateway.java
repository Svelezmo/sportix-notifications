package com.sportix.notifications.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Modo desarrollo (notifications.mail.mode=console, el valor por defecto):
 * no envía nada, imprime el correo en la consola. Sirve para probar el login
 * con 2FA sin configurar Gmail: el código se lee en la consola de este servicio.
 */
@Component
@ConditionalOnProperty(name = "notifications.mail.mode", havingValue = "console", matchIfMissing = true)
public class ConsoleMailGateway implements MailGateway {

    private static final Logger log = LoggerFactory.getLogger(ConsoleMailGateway.class);

    public ConsoleMailGateway() {
        log.warn("Correo en MODO CONSOLA: los códigos se imprimen aquí y NO se envían. Para Gmail use MAIL_MODE=smtp");
    }

    @Override
    public String send(EmailMessage message) {
        String id = "console-" + UUID.randomUUID();
        log.info("""

                ==================== CORREO (modo consola) ====================
                Para:    {}
                Asunto:  {}
                ---------------------------------------------------------------
                {}
                ===============================================================""",
                message.to(), message.subject(), message.textBody());
        return id;
    }

    @Override
    public boolean isDryRun() {
        return true;
    }
}
