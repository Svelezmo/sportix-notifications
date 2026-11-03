package com.sportix.notifications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del servicio (todo viene de application.properties / .env).
 *
 * @param apiKey clave que debe traer cada petición en el header X-API-Key
 *               (vacía = no se exige, solo para desarrollo local)
 * @param mail   cómo se entregan los correos
 */
@ConfigurationProperties(prefix = "notifications")
public record NotificationProperties(String apiKey, Mail mail) {

    /**
     * @param mode     smtp (envío real) o console (se imprime en la consola)
     * @param fromName nombre visible del remitente
     * @param async    true = responde a Sportixcore apenas valida la petición y
     *                 envía el correo en segundo plano (no espera a Gmail)
     */
    public record Mail(String mode, String fromName, boolean async) {
    }

    public boolean apiKeyRequired() {
        return apiKey != null && !apiKey.isBlank();
    }
}
