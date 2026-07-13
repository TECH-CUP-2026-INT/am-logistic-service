package co.edu.escuelaing.techcup.logistics.security;

import co.edu.escuelaing.techcup.logistics.config.InternalApiKeyProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica llamadas servicio-a-servicio (p.ej. Torneos, Equipos, Auditoria) mediante
 * una API key interna compartida, igual que en notification-service. Si el header no
 * viene o no coincide, no autentica: la cadena de seguridad rechaza con 401.
 */
@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_NAME = "X-Internal-Api-Key";

    private final InternalApiKeyProperties properties;

    public InternalApiKeyFilter(InternalApiKeyProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader(HEADER_NAME);
        if (apiKey != null && apiKey.equals(properties.apiKey())
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    new InternalServicePrincipal(), null, List.of(new SimpleGrantedAuthority("ROLE_SERVICIO_INTERNO")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}
