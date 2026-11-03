package com.sportix.notifications.service;

import com.sportix.notifications.mail.EmailMessage;
import com.sportix.notifications.model.CodePurpose;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * Arma el correo del código (texto plano + HTML) según el propósito.
 * Separado del envío (responsabilidad única): cambiar el diseño del correo no
 * toca la forma de enviarlo, y al revés.
 */
@Component
public class VerificationEmailTemplate {

    public EmailMessage build(String to, String code, int ttlMinutes, CodePurpose purpose) {
        String subject = "Sportix - " + purpose.getSubject();
        String minutes = ttlMinutes == 1 ? "1 minuto" : ttlMinutes + " minutos";

        String text = """
                Hola,

                %s

                Código: %s

                Vence en %s. Si no fuiste tú, ignora este correo: nadie puede entrar sin este código.

                Sportix
                """.formatted(purpose.getInstruction(), code, minutes);

        // El código va separado en dígitos grandes para leerlo y copiarlo fácil
        String html = """
                <!doctype html>
                <html lang="es"><body style="margin:0;padding:24px;background:#0f4d33;font-family:Arial,Helvetica,sans-serif;">
                  <table role="presentation" width="100%%" style="max-width:480px;margin:0 auto;background:#fbfcf7;border:2px solid #0a3826;border-radius:6px;">
                    <tr><td style="padding:28px 28px 8px;">
                      <p style="margin:0;font-size:26px;font-weight:bold;color:#0f4d33;">Sportix</p>
                      <p style="margin:16px 0 0;font-size:16px;color:#10261b;">%s</p>
                    </td></tr>
                    <tr><td align="center" style="padding:20px 28px;">
                      <div style="display:inline-block;padding:14px 22px;background:#ffcb1f;border:2px solid #0a3826;border-radius:6px;font-size:34px;letter-spacing:10px;font-weight:bold;color:#10261b;font-family:'Courier New',monospace;">%s</div>
                    </td></tr>
                    <tr><td style="padding:0 28px 28px;font-size:14px;color:#46604f;">
                      Vence en <strong>%s</strong>. Si no fuiste tú, ignora este correo: nadie puede entrar sin este código.
                    </td></tr>
                  </table>
                </body></html>
                """.formatted(HtmlUtils.htmlEscape(purpose.getInstruction()), HtmlUtils.htmlEscape(code), minutes);

        return new EmailMessage(to, subject, text, html);
    }
}
