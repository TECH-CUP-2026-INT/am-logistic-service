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
import co.edu.escuelaing.techcup.logistics.dto.request.CrearDefinicionRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.ItemRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.DefinicionRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.logistics.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.logistics.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.logistics.security.OrganizadorInterceptor;
import co.edu.escuelaing.techcup.logistics.service.DefinicionRefrigerioService;
import co.edu.escuelaing.techcup.logistics.support.JwtTestSupport;

@WebMvcTest(DefinicionRefrigerioController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class,
        OrganizadorInterceptor.class, WebMvcConfig.class, CurrentUserProvider.class})
@EnableConfigurationProperties({RoleClaimProperties.class, InternalApiKeyProperties.class})
class DefinicionRefrigerioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @MockitoBean
    private DefinicionRefrigerioService service;

    @Test
    void crear_conRolOrganizador_retorna201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID partidoId = UUID.randomUUID();
        UUID equipoId = UUID.randomUUID();
        CrearDefinicionRefrigerioRequest request = new CrearDefinicionRefrigerioRequest(
                partidoId, equipoId, List.of(new ItemRefrigerioRequest("Sandwich", 10)), null);

        when(service.crear(any(), any())).thenReturn(new DefinicionRefrigerioResponse(
                UUID.randomUUID(), partidoId, equipoId, List.of(), null, userId, Instant.now()));

        mockMvc.perform(post("/api/refrigerios/definiciones")
                        .header("Authorization", JwtTestSupport.bearer(userId, "ORGANIZADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.partidoId").value(partidoId.toString()));
    }

    @Test
    void crear_sinRolOrganizador_retorna403() throws Exception {
        UUID userId = UUID.randomUUID();
        CrearDefinicionRefrigerioRequest request = new CrearDefinicionRefrigerioRequest(
                UUID.randomUUID(), UUID.randomUUID(), List.of(new ItemRefrigerioRequest("Sandwich", 10)), null);

        mockMvc.perform(post("/api/refrigerios/definiciones")
                        .header("Authorization", JwtTestSupport.bearer(userId, "JUGADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void crear_sinAutenticacion_retornaNoAutorizado() throws Exception {
        CrearDefinicionRefrigerioRequest request = new CrearDefinicionRefrigerioRequest(
                UUID.randomUUID(), UUID.randomUUID(), List.of(new ItemRefrigerioRequest("Sandwich", 10)), null);

        mockMvc.perform(post("/api/refrigerios/definiciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void listarPorPartido_conRolOrganizador_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID partidoId = UUID.randomUUID();
        when(service.listarPorPartido(partidoId)).thenReturn(List.of());

        mockMvc.perform(get("/api/refrigerios/definiciones")
                        .header("Authorization", JwtTestSupport.bearer(userId, "ORGANIZADOR"))
                        .param("partidoId", partidoId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorPartido_sinRolAutorizado_retorna403() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID partidoId = UUID.randomUUID();

        mockMvc.perform(get("/api/refrigerios/definiciones")
                        .header("Authorization", JwtTestSupport.bearer(userId, "JUGADOR"))
                        .param("partidoId", partidoId.toString()))
                .andExpect(status().isForbidden());
    }
}
