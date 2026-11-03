package com.sportix.notifications.exception;

import com.sportix.notifications.dto.SendCodeResponse;
import com.sportix.notifications.mail.MailDeliveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Todos los errores con el mismo formato { "status": "error", "message": ... }.
 *   400 -> datos inválidos (correo, código, ttl o purpose)
 *   502 -> Gmail no aceptó el correo (contraseña de aplicación mala, sin internet...)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SendCodeResponse> invalid(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(SendCodeResponse.error("Datos inválidos", null, errors));
    }

    /** JSON mal formado o un "purpose" que no existe */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<SendCodeResponse> unreadable(HttpMessageNotReadableException exception) {
        Throwable cause = exception.getMostSpecificCause();
        String message = cause instanceof IllegalArgumentException ? cause.getMessage() : "El cuerpo no es un JSON válido";
        return ResponseEntity.badRequest().body(SendCodeResponse.error(message, null, null));
    }

    @ExceptionHandler(MailDeliveryException.class)
    public ResponseEntity<SendCodeResponse> delivery(MailDeliveryException exception) {
        log.error("No se pudo entregar el correo: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(SendCodeResponse.error("El proveedor de correo no aceptó el mensaje", null, null));
    }
}
