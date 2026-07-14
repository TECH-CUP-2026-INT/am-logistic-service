package co.edu.escuelaing.techcup.logistics.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ForbiddenRoleExceptionTest {

    @Test
    void constructor_setsMessage() {
        ForbiddenRoleException exception = new ForbiddenRoleException("Esta operacion requiere el rol organizador");

        assertThat(exception.getMessage()).isEqualTo("Esta operacion requiere el rol organizador");
    }
}
