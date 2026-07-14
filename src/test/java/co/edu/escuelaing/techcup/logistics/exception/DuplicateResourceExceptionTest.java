package co.edu.escuelaing.techcup.logistics.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DuplicateResourceExceptionTest {

    @Test
    void constructor_setsMessage() {
        DuplicateResourceException exception = new DuplicateResourceException("Ya existe una entrega");

        assertThat(exception.getMessage()).isEqualTo("Ya existe una entrega");
    }
}
