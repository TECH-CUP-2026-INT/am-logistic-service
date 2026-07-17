package co.edu.escuelaing.techcup.logistics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.escuelaing.techcup.logistics.config.InternalApiKeyProperties;
import co.edu.escuelaing.techcup.logistics.config.RoleClaimProperties;
import co.edu.escuelaing.techcup.logistics.config.SecurityConfig;
import co.edu.escuelaing.techcup.logistics.config.WebMvcConfig;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarEntregaRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.EntregaRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.logistics.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.logistics.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.logistics.security.OrganizadorInterceptor;
import co.edu.escuelaing.techcup.logistics.service.EntregaRefrigerioService;
import co.edu.escuelaing.techcup.logistics.support.JwtTestSupport;

@WebMvcTest(EntregaRefrigerioController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class,
        OrganizadorInterceptor.class, WebMvcConfig.class, CurrentUserProvider.class})
@EnableConfigurationProperties({RoleClaimProperties.class, InternalApiKeyProperties.class})
class EntregaRefrigerioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @MockitoBean
    private EntregaRefrigerioService service;

    @Test
    void registrar_conRolOrganizador_retorna201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID partidoId = UUID.randomUUID();
        UUID equipoId = UUID.randomUUID();
        UUID capitanId = UUID.randomUUID();
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                UUID.randomUUID(), capitanId, null);

        when(service.registrar(any(), any())).thenReturn(new EntregaRefrigerioResponse(
                UUID.randomUUID(), request.definicionRefrigerioId(), partidoId,
                equipoId, capitanId, userId, Instant.now(), null));

        mockMvc.perform(post("/api/refrigerios/entregas")
                        .header("Authorization", JwtTestSupport.bearer(userId, "ORGANIZADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.responsableId").value(userId.toString()))
                .andExpect(jsonPath("$.capitanId").value(capitanId.toString()));
    }

    @Test
    void registrar_sinRolOrganizador_retorna403() throws Exception {
        UUID userId = UUID.randomUUID();
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                UUID.randomUUID(), UUID.randomUUID(), null);

        mockMvc.perform(post("/api/refrigerios/entregas")
                        .header("Authorization", JwtTestSupport.bearer(userId, "JUGADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrar_sinAutenticacion_retornaNoAutorizado() throws Exception {
        RegistrarEntregaRefrigerioRequest request = new RegistrarEntregaRefrigerioRequest(
                UUID.randomUUID(), UUID.randomUUID(), null);

        mockMvc.perform(post("/api/refrigerios/entregas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void listarPorPartido_conRolOrganizador_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID partidoId = UUID.randomUUID();
        when(service.listarPorPartido(partidoId)).thenReturn(List.of());

        mockMvc.perform(get("/api/refrigerios/entregas")
                        .header("Authorization", JwtTestSupport.bearer(userId, "ORGANIZADOR"))
                        .param("partidoId", partidoId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorPartido_sinRolAutorizado_retorna403() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID partidoId = UUID.randomUUID();

        mockMvc.perform(get("/api/refrigerios/entregas")
                        .header("Authorization", JwtTestSupport.bearer(userId, "JUGADOR"))
                        .param("partidoId", partidoId.toString()))
                .andExpect(status().isForbidden());
    }
}
