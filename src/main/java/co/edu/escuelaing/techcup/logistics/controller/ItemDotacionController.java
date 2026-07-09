package co.edu.escuelaing.techcup.logistics.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.escuelaing.techcup.logistics.dto.request.MarcarDotacionEntregadaRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarItemDotacionRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.ItemDotacionResponse;
import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;
import co.edu.escuelaing.techcup.logistics.security.CurrentUser;
import co.edu.escuelaing.techcup.logistics.security.RequestHeaders;
import co.edu.escuelaing.techcup.logistics.security.RequireOrganizador;
import co.edu.escuelaing.techcup.logistics.service.ItemDotacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Requerimiento 3: registro y control de entrega de dotacion (petos, balones, kits).
 */
@RestController
@RequestMapping("/api/dotacion")
@RequiredArgsConstructor
public class ItemDotacionController {

    private final ItemDotacionService service;

    @PostMapping
    @RequireOrganizador
    @Operation(
            summary = "Registra un item de dotacion como PENDIENTE",
            description = "Requiere rol organizador. El header X-User-Role no viaja como parametro "
                    + "del metodo: lo valida el interceptor de seguridad antes de llegar aqui.",
            parameters = @Parameter(name = RequestHeaders.X_USER_ROLE, in = ParameterIn.HEADER,
                    required = true, example = RequestHeaders.ROLE_ORGANIZADOR)
    )
    public ResponseEntity<ItemDotacionResponse> registrar(
            @Valid @RequestBody RegistrarItemDotacionRequest request) {
        ItemDotacionResponse response = service.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{itemId}/entrega")
    @RequireOrganizador
    @Operation(
            summary = "Marca un item de dotacion como ENTREGADO",
            description = "Requiere rol organizador. El header X-User-Role no viaja como parametro "
                    + "del metodo: lo valida el interceptor de seguridad antes de llegar aqui.",
            parameters = @Parameter(name = RequestHeaders.X_USER_ROLE, in = ParameterIn.HEADER,
                    required = true, example = RequestHeaders.ROLE_ORGANIZADOR)
    )
    public ResponseEntity<ItemDotacionResponse> marcarEntregado(
            @PathVariable UUID itemId,
            @Valid @RequestBody(required = false) MarcarDotacionEntregadaRequest request,
            @RequestHeader(RequestHeaders.X_USER_ID) String userId) {
        UUID entregadoPorId = CurrentUser.idFrom(userId);
        MarcarDotacionEntregadaRequest body = request != null ? request : new MarcarDotacionEntregadaRequest(null);
        return ResponseEntity.ok(service.marcarEntregado(itemId, body, entregadoPorId));
    }

    @GetMapping
    public ResponseEntity<List<ItemDotacionResponse>> listarPorEquipo(
            @RequestParam UUID equipoId,
            @RequestParam(required = false) EstadoDotacion estado) {
        return ResponseEntity.ok(service.listarPorEquipo(equipoId, estado));
    }
}
