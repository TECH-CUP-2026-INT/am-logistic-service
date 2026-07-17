package co.edu.escuelaing.techcup.logistics.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.escuelaing.techcup.logistics.dto.request.MarcarDotacionEntregadaRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarDevolucionDotacionRequest;
import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarItemDotacionRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.ItemDotacionResponse;
import co.edu.escuelaing.techcup.logistics.enums.EstadoDotacion;
import co.edu.escuelaing.techcup.logistics.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.logistics.security.RequireOrganizador;
import co.edu.escuelaing.techcup.logistics.service.ItemDotacionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Requerimiento 3: registro y control de entrega de dotacion (petos, balones,
 * kits) a arbitros, con rastreo individual por unidad fisica.
 */
@RestController
@RequestMapping("/api/dotacion")
@RequiredArgsConstructor
public class ItemDotacionController {

    private final ItemDotacionService service;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @RequireOrganizador
    @Operation(
            summary = "Registra items de dotacion como PENDIENTE, uno por unidad fisica",
            description = "Requiere rol organizador. arbitroId se valida contra el Servicio de "
                    + "Equipos: la dotacion solo puede entregarse a un arbitro. Se crea un item "
                    + "independiente por cada unidad indicada en 'cantidad', para poder rastrear "
                    + "cada uno individualmente en las etapas de entrega y devolucion. La identidad "
                    + "y el rol se extraen del JWT (header Authorization: Bearer) ya validado por el "
                    + "API Gateway; el interceptor de seguridad rechaza la solicitud si el rol no es "
                    + "organizador."
    )
    public ResponseEntity<List<ItemDotacionResponse>> registrar(
            @Valid @RequestBody RegistrarItemDotacionRequest request) {
        List<ItemDotacionResponse> response = service.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{itemId}/entrega")
    @RequireOrganizador
    @Operation(
            summary = "Marca un item de dotacion como ENTREGADO",
            description = "Requiere rol organizador. La identidad y el rol se extraen del JWT "
                    + "(header Authorization: Bearer) ya validado por el API Gateway; el "
                    + "interceptor de seguridad rechaza la solicitud si el rol no es organizador."
    )
    public ResponseEntity<ItemDotacionResponse> marcarEntregado(
            @PathVariable UUID itemId,
            @Valid @RequestBody(required = false) MarcarDotacionEntregadaRequest request) {
        UUID entregadoPorId = currentUserProvider.getCurrentUserId();
        MarcarDotacionEntregadaRequest body = request != null ? request : new MarcarDotacionEntregadaRequest(null);
        return ResponseEntity.ok(service.marcarEntregado(itemId, body, entregadoPorId));
    }

    @PatchMapping("/{itemId}/devolucion")
    @RequireOrganizador
    @Operation(
            summary = "Registra la devolucion de un item de dotacion (estado DEVUELTO)",
            description = "Requiere rol organizador. El Arbitro devuelve fisicamente los items y el "
                    + "Organizador autenticado registra la devolucion. Solo aplica si el item esta "
                    + "actualmente en estado ENTREGADO. La identidad y el rol se extraen del JWT "
                    + "(header Authorization: Bearer) ya validado por el API Gateway; el "
                    + "interceptor de seguridad rechaza la solicitud si el rol no es organizador."
    )
    public ResponseEntity<ItemDotacionResponse> registrarDevolucion(
            @PathVariable UUID itemId,
            @Valid @RequestBody(required = false) RegistrarDevolucionDotacionRequest request) {
        UUID recibidoPorId = currentUserProvider.getCurrentUserId();
        RegistrarDevolucionDotacionRequest body = request != null ? request
                : new RegistrarDevolucionDotacionRequest(null);
        return ResponseEntity.ok(service.registrarDevolucion(itemId, body, recibidoPorId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZADOR')")
    @Operation(summary = "Lista los items de dotacion de un arbitro", description = "Requiere rol ADMIN u ORGANIZADOR.")
    public ResponseEntity<List<ItemDotacionResponse>> listarPorArbitro(
            @RequestParam UUID arbitroId,
            @RequestParam(required = false) EstadoDotacion estado) {
        return ResponseEntity.ok(service.listarPorArbitro(arbitroId, estado));
    }
}
