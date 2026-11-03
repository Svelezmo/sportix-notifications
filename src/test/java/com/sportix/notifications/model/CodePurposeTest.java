package com.sportix.notifications.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodePurposeTest {

    @Test
    void acceptsSportixcoreCodes() {
        assertThat(CodePurpose.from("login_2fa")).isEqualTo(CodePurpose.LOGIN_2FA);
        assertThat(CodePurpose.from("PASSWORD_RESET")).isEqualTo(CodePurpose.PASSWORD_RESET);
    }

    @Test
    void rejectsUnknown() {
        assertThatThrownBy(() -> CodePurpose.from("marketing")).hasMessageContaining("purpose no válido");
    }
}
