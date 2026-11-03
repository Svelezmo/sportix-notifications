package com.sportix.notifications.security;

import com.sportix.notifications.config.NotificationProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Protege /api/**: solo quien conoce la clave (Sportixcore) puede pedir envíos.
 * Sin esto, cualquiera que encuentre el puerto podría mandar correos a
 * nombre de Sportix desde la cuenta de Gmail del equipo.
 *
 * La clave viaja en el header X-API-Key y se compara en tiempo constante.
 * /health queda libre para comprobar que el servicio está arriba.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-API-Key";

    private final NotificationProperties properties;

    public ApiKeyFilter(NotificationProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.apiKeyRequired() || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER);
        if (provided != null && MessageDigest.isEqual(
                provided.getBytes(StandardCharsets.UTF_8),
                properties.apiKey().getBytes(StandardCharsets.UTF_8))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\":\"error\",\"message\":\"Falta el header X-API-Key o no es correcto\"}");
    }
}
