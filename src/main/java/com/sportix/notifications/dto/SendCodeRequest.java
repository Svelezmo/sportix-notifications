package com.sportix.notifications.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sportix.notifications.model.CodePurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * POST /api/v1/2fa/send – exactamente lo que ya envía Sportixcore
 * (NotificationServiceVerificationCodeNotifier), así no hubo que cambiar el contrato:
 *
 *   { "to": "ana@mail.com", "code": "123456", "ttl_minutes": 5,
 *     "request_id": "9b1c...", "purpose": "login_2fa" }
 */
public record SendCodeRequest(

        @NotBlank(message = "El destinatario (to) es obligatorio")
        @Email(message = "El destinatario (to) no es un correo válido")
        String to,

        @NotBlank(message = "El código (code) es obligatorio")
        @Pattern(regexp = "^\\d{6}$", message = "El código debe tener exactamente 6 dígitos")
        String code,

        @JsonProperty("ttl_minutes")
        @NotNull(message = "ttl_minutes es obligatorio")
        @Positive(message = "ttl_minutes debe ser mayor que 0")
        @Max(value = 60, message = "ttl_minutes no puede superar 60")
        Integer ttlMinutes,

        @JsonProperty("request_id")
        @Size(max = 100, message = "request_id no puede superar 100 caracteres")
        String requestId,

        /** Opcional: si no viene se asume login_2fa */
        CodePurpose purpose) {

    public CodePurpose purposeOrDefault() {
        return purpose == null ? CodePurpose.LOGIN_2FA : purpose;
    }
}
