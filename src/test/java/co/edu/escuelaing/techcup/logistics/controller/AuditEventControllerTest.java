package co.edu.escuelaing.techcup.logistics.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.escuelaing.techcup.logistics.config.InternalApiKeyProperties;
import co.edu.escuelaing.techcup.logistics.config.RoleClaimProperties;
import co.edu.escuelaing.techcup.logistics.config.SecurityConfig;
import co.edu.escuelaing.techcup.logistics.config.WebMvcConfig;
import co.edu.escuelaing.techcup.logistics.dto.response.AuditEventResponse;
import co.edu.escuelaing.techcup.logistics.enums.TipoEventoAuditoria;
import co.edu.escuelaing.techcup.logistics.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.logistics.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.logistics.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.logistics.security.OrganizadorInterceptor;
import co.edu.escuelaing.techcup.logistics.service.AuditEventService;
import co.edu.escuelaing.techcup.logistics.support.JwtTestSupport;

@WebMvcTest(AuditEventController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class,
        OrganizadorInterceptor.class, WebMvcConfig.class, CurrentUserProvider.class})
@EnableConfigurationProperties({RoleClaimProperties.class, InternalApiKeyProperties.class})
class AuditEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditEventService service;

    @Test
    void listarEventos_conRolOrganizador_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(service.listarEventos()).thenReturn(List.of(new AuditEventResponse(
                TipoEventoAuditoria.DOTACION_ENTREGADA, UUID.randomUUID(), UUID.randomUUID(),
                Instant.now(), "detalle")));

        mockMvc.perform(get("/api/auditoria/eventos")
                        .header("Authorization", JwtTestSupport.bearer(userId, "ORGANIZADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("DOTACION_ENTREGADA"));
    }

    @Test
    void listarEventos_conRolAdmin_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        when(service.listarEventos()).thenReturn(List.of());

        mockMvc.perform(get("/api/auditoria/eventos")
                        .header("Authorization", JwtTestSupport.bearer(userId, "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void listarEventos_conRolNoAutorizado_retorna403() throws Exception {
        UUID userId = UUID.randomUUID();

        mockMvc.perform(get("/api/auditoria/eventos")
                        .header("Authorization", JwtTestSupport.bearer(userId, "JUGADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarEventos_sinAutenticacion_retornaNoAutorizado() throws Exception {
        mockMvc.perform(get("/api/auditoria/eventos"))
                .andExpect(status().is4xxClientError());
    }
}
