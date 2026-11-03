package com.sportix.notifications.controller;

import com.sportix.notifications.mail.MailGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** GET /health – para comprobar rápido que el servicio está arriba y en qué modo. */
@RestController
public class HealthController {

    private final MailGateway mailGateway;

    public HealthController(MailGateway mailGateway) {
        this.mailGateway = mailGateway;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "mode", mailGateway.isDryRun() ? "console" : "smtp");
    }
}
