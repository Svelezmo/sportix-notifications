package com.sportix.notifications.mail;

/** Un correo listo para enviar: texto plano + HTML (los clientes eligen cuál mostrar). */
public record EmailMessage(String to, String subject, String textBody, String htmlBody) {
}
