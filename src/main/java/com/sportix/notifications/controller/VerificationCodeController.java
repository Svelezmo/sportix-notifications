package com.sportix.notifications.controller;

import com.sportix.notifications.dto.SendCodeRequest;
import com.sportix.notifications.dto.SendCodeResponse;
import com.sportix.notifications.service.VerificationCodeSender;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/v1/2fa/send – única operación del servicio.
 * Requiere el header X-API-Key (ApiKeyFilter) cuando hay una clave configurada.
 */
@RestController
@RequestMapping("/api/v1/2fa")
public class VerificationCodeController {

    private final VerificationCodeSender sender;

    public VerificationCodeController(VerificationCodeSender sender) {
        this.sender = sender;
    }

    @PostMapping("/send")
    public SendCodeResponse send(@Valid @RequestBody SendCodeRequest request) {
        return sender.send(request);
    }
}
