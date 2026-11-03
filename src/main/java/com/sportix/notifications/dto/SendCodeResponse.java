package com.sportix.notifications.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Respuesta del servicio. Sportixcore revisa que "status" sea "ok".
 *   200 -> { "status": "ok", "message_id": "...", "dry_run": false, "request_id": "..." }
 *   4xx/5xx -> { "status": "error", "message": "...", "errors": {...}, "request_id": "..." }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SendCodeResponse(
        String status,
        @JsonProperty("message_id") String messageId,
        @JsonProperty("dry_run") Boolean dryRun,
        @JsonProperty("request_id") String requestId,
        String message,
        Map<String, String> errors) {

    public static SendCodeResponse ok(String messageId, boolean dryRun, String requestId) {
        return new SendCodeResponse("ok", messageId, dryRun, requestId, null, null);
    }

    public static SendCodeResponse error(String message, String requestId, Map<String, String> errors) {
        return new SendCodeResponse("error", null, null, requestId, message, errors);
    }
}
