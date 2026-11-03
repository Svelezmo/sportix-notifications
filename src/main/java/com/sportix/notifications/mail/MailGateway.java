package com.sportix.notifications.mail;

/**
 * Por dónde sale el correo. El servicio depende de esta interfaz y no de
 * Gmail (inversión de dependencias): cambiar a otro proveedor es agregar
 * otra implementación, sin tocar la lógica.
 */
public interface MailGateway {

    /**
     * @return identificador del mensaje enviado (para rastrearlo en los logs)
     * @throws MailDeliveryException si el proveedor no aceptó el correo
     */
    String send(EmailMessage message);

    /** true si el correo no sale de verdad (modo consola) */
    boolean isDryRun();
}
