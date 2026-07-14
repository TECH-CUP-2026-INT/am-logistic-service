package co.edu.escuelaing.techcup.logistics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import co.edu.escuelaing.techcup.logistics.dto.request.MarcarDotacionEntregadaRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarDevolucionDotacionRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarItemDotacionRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.ItemDotacionResponse;
import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;
import co.edu.escuelaing.techcup.logistics.enums.TipoItemDotacion;
import co.edu.escuelaing.techcup.logistics.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.logistics.security.InternalApiKeyFilter;
import co.edu.escuelaing.techcup.logistics.security.JwtClaimsFilter;
import co.edu.escuelaing.techcup.logistics.security.OrganizadorInterceptor;
import co.edu.escuelaing.techcup.logistics.service.ItemDotacionService;
import co.edu.escuelaing.techcup.logistics.support.JwtTestSupport;

@WebMvcTest(ItemDotacionController.class)
@Import({SecurityConfig.class, JwtClaimsFilter.class, InternalApiKeyFilter.class,
        OrganizadorInterceptor.class, WebMvcConfig.class, CurrentUserProvider.class})
@EnableConfigurationProperties({RoleClaimProperties.class, InternalApiKeyProperties.class})
class ItemDotacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @MockitoBean
    private ItemDotacionService service;

    @Test
    void registrar_conRolOrganizador_retorna201() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID equipoId = UUID.randomUUID();
        RegistrarItemDotacionRequest request = new RegistrarItemDotacionRequest(
                equipoId, TipoItemDotacion.PETO, 10, UUID.randomUUID(), null);

        when(service.registrar(any())).thenReturn(new ItemDotacionResponse(
                UUID.randomUUID(), equipoId, TipoItemDotacion.PETO, 10, EstadoDotacion.PENDIENTE,
                request.responsableAsignadoId(), null, Instant.now(), null, null, null, null));

        mockMvc.perform(post("/api/dotacion")
                        .header("Authorization", JwtTestSupport.bearer(userId, "ORGANIZADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void registrar_sinRolOrganizador_retorna403() throws Exception {
        UUID userId = UUID.randomUUID();
        RegistrarItemDotacionRequest request = new RegistrarItemDotacionRequest(
                UUID.randomUUID(), TipoItemDotacion.BALON, 5, UUID.randomUUID(), null);

        mockMvc.perform(post("/api/dotacion")
                        .header("Authorization", JwtTestSupport.bearer(userId, "JUGADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrar_sinAutenticacion_retornaNoAutorizado() throws Exception {
        RegistrarItemDotacionRequest request = new RegistrarItemDotacionRequest(
                UUID.randomUUID(), TipoItemDotacion.BALON, 5, UUID.randomUUID(), null);

        mockMvc.perform(post("/api/dotacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void marcarEntregado_conRolOrganizador_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        MarcarDotacionEntregadaRequest request = new MarcarDotacionEntregadaRequest("Entregado");

        when(service.marcarEntregado(any(), any(), any())).thenReturn(new ItemDotacionResponse(
                itemId, UUID.randomUUID(), TipoItemDotacion.KIT, 1, EstadoDotacion.ENTREGADO,
                UUID.randomUUID(), userId, Instant.now(), Instant.now(), null, null, "Entregado"));

        mockMvc.perform(patch("/api/dotacion/{itemId}/entrega", itemId)
                        .header("Authorization", JwtTestSupport.bearer(userId, "ORGANIZADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));
    }

    @Test
    void marcarEntregado_sinCuerpo_usaRequestVacio() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        when(service.marcarEntregado(any(), any(), any())).thenReturn(new ItemDotacionResponse(
                itemId, UUID.randomUUID(), TipoItemDotacion.KIT, 1, EstadoDotacion.ENTREGADO,
                UUID.randomUUID(), userId, Instant.now(), Instant.now(), null, null, null));

        mockMvc.perform(patch("/api/dotacion/{itemId}/entrega", itemId)
                        .header("Authorization", JwtTestSupport.bearer(userId, "ORGANIZADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void marcarEntregado_sinRolOrganizador_retorna403() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(patch("/api/dotacion/{itemId}/entrega", itemId)
                        .header("Authorization", JwtTestSupport.bearer(userId, "JUGADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarDevolucion_conRolOrganizador_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        RegistrarDevolucionDotacionRequest request = new RegistrarDevolucionDotacionRequest("Devuelto en buen estado");

        when(service.registrarDevolucion(any(), any(), any())).thenReturn(new ItemDotacionResponse(
                itemId, UUID.randomUUID(), TipoItemDotacion.KIT, 1, EstadoDotacion.DEVUELTO,
                UUID.randomUUID(), UUID.randomUUID(), Instant.now(), Instant.now(), userId, Instant.now(),
                "Devuelto en buen estado"));

        mockMvc.perform(patch("/api/dotacion/{itemId}/devolucion", itemId)
                        .header("Authorization", JwtTestSupport.bearer(userId, "ORGANIZADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTO"));
    }

    @Test
    void registrarDevolucion_sinCuerpo_usaRequestVacio() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        when(service.registrarDevolucion(any(), any(), any())).thenReturn(new ItemDotacionResponse(
                itemId, UUID.randomUUID(), TipoItemDotacion.KIT, 1, EstadoDotacion.DEVUELTO,
                UUID.randomUUID(), UUID.randomUUID(), Instant.now(), Instant.now(), userId, Instant.now(), null));

        mockMvc.perform(patch("/api/dotacion/{itemId}/devolucion", itemId)
                        .header("Authorization", JwtTestSupport.bearer(userId, "ORGANIZADOR")))
                .andExpect(status().isOk());
    }

    @Test
    void registrarDevolucion_sinRolOrganizador_retorna403() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        mockMvc.perform(patch("/api/dotacion/{itemId}/devolucion", itemId)
                        .header("Authorization", JwtTestSupport.bearer(userId, "JUGADOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarPorEquipo_autenticadoSinRolOrganizador_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID equipoId = UUID.randomUUID();
        when(service.listarPorEquipo(equipoId, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/dotacion")
                        .header("Authorization", JwtTestSupport.bearer(userId, "JUGADOR"))
                        .param("equipoId", equipoId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void listarPorEquipo_conFiltroDeEstado_retorna200() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID equipoId = UUID.randomUUID();
        when(service.listarPorEquipo(equipoId, EstadoDotacion.PENDIENTE)).thenReturn(List.of());

        mockMvc.perform(get("/api/dotacion")
                        .header("Authorization", JwtTestSupport.bearer(userId, "JUGADOR"))
                        .param("equipoId", equipoId.toString())
                        .param("estado", "PENDIENTE"))
                .andExpect(status().isOk());
    }
}
