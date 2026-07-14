package co.edu.escuelaing.techcup.logistics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "techcup.security")
public record RoleClaimProperties(String roleClaim) {

    public RoleClaimProperties {
        if (roleClaim == null || roleClaim.isBlank()) {
            roleClaim = "roles";
        }
    }
}
