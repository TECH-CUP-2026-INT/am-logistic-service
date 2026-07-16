package co.edu.escuelaing.techcup.logistics.config;

import co.edu.escuelaing.techcup.logistics.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.logistics.security.JwtClaimsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtClaimsFilter jwtClaimsFilter;
    private final InternalApiKeyFilter internalApiKeyFilter;

    public SecurityConfig(JwtClaimsFilter jwtClaimsFilter, InternalApiKeyFilter internalApiKeyFilter) {
        this.jwtClaimsFilter = jwtClaimsFilter;
        this.internalApiKeyFilter = internalApiKeyFilter;
    }

    @Bean
    @SuppressWarnings("java:S4502") // CSRF no aplica: API sin estado (STATELESS), autenticada por
    // JWT Bearer / API key interna en headers explicitos, sin cookies de sesion que un navegador
    // adjunte automaticamente; revisado y confirmado seguro para esta arquitectura.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtClaimsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
