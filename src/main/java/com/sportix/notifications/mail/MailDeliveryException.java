package com.sportix.notifications.mail;

/** El proveedor de correo rechazó o no respondió: se traduce en 502 para Sportixcore. */
public class MailDeliveryException extends RuntimeException {

    public MailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
