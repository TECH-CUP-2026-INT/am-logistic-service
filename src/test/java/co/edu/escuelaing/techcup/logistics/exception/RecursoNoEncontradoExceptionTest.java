package co.edu.escuelaing.techcup.logistics.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RecursoNoEncontradoExceptionTest {

    @Test
    void constructor_setsMessage() {
        RecursoNoEncontradoException exception = new RecursoNoEncontradoException("Partido no encontrado");

        assertThat(exception.getMessage()).isEqualTo("Partido no encontrado");
    }
}
