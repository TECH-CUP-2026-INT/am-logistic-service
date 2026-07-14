package co.edu.escuelaing.techcup.logistics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Habilita @Async, usado por el adaptador de Auditoria para reportar entregas
 * en un hilo separado sin bloquear ni afectar el flujo principal.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
