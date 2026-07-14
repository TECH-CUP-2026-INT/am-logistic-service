package co.edu.escuelaing.techcup.logistics.security;

import java.util.Set;
import java.util.UUID;

/**
 * Principal de seguridad construido a partir de los claims del JWT ya validado por el
 * API Gateway (userId + roles), igual que en matches-service y notification-service.
 */
public record AuthenticatedUser(UUID userId, Set<String> roles) {
}
