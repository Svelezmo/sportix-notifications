package com.sportix.notifications.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Para qué es el código. Sportixcore lo envía en el campo "purpose" y con él
 * se elige el texto del correo (el código es el mismo tipo de dato en los dos casos).
 */
public enum CodePurpose {

    LOGIN_2FA("login_2fa", "Tu código para iniciar sesión", "Usa este código para terminar de iniciar sesión en Sportix."),
    PASSWORD_RESET("password_reset", "Tu código para cambiar la contraseña", "Usa este código para definir una contraseña nueva en Sportix.");

    private final String code;
    private final String subject;
    private final String instruction;

    CodePurpose(String code, String subject, String instruction) {
        this.code = code;
        this.subject = subject;
        this.instruction = instruction;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getSubject() {
        return subject;
    }

    public String getInstruction() {
        return instruction;
    }

    /** "login_2fa" o "LOGIN_2FA" -> LOGIN_2FA. Un valor desconocido responde 400. */
    @JsonCreator
    public static CodePurpose from(String value) {
        return Arrays.stream(values())
                .filter(p -> p.code.equalsIgnoreCase(value) || p.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "purpose no válido: '" + value + "'. Use login_2fa o password_reset"));
    }
}
