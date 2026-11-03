package com.sportix.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * sportix-notifications: entrega por correo los códigos de verificación que
 * GENERA Sportixcore (login con 2FA y recuperación de contraseña).
 *
 * Este servicio no crea ni valida códigos: solo los envía. Así la lógica de
 * seguridad (generar, guardar el hash, contar intentos, vencer) vive en un solo
 * lugar, Sportixcore, y este servicio se puede cambiar sin tocarla.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class NotificationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationsApplication.class, args);
    }
}
