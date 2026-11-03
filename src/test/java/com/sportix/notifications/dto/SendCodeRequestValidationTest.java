package com.sportix.notifications.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/** Las mismas reglas que responde el servicio con 400. */
class SendCodeRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequest() {
        assertThat(fields(new SendCodeRequest("ana@mail.com", "123456", 5, "r", null))).isEmpty();
    }

    @Test
    void invalidFields() {
        assertThat(fields(new SendCodeRequest("no-es-correo", "12ab", 0, null, null)))
                .containsExactlyInAnyOrder("to", "code", "ttlMinutes");
        assertThat(fields(new SendCodeRequest(null, null, null, null, null)))
                .containsExactlyInAnyOrder("to", "code", "ttlMinutes");
    }

    private Set<String> fields(SendCodeRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath).map(Object::toString).collect(Collectors.toSet());
    }
}
