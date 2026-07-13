package co.edu.escuelaing.techcup.logistics.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IntegrationExceptionTest {

    @Test
    void constructor_setsMessageAndCause() {
        RuntimeException cause = new RuntimeException("fallo de red");

        IntegrationException exception = new IntegrationException("No fue posible integrar", cause);

        assertThat(exception.getMessage()).isEqualTo("No fue posible integrar");
        assertThat(exception.getCause()).isSameAs(cause);
    }
}
