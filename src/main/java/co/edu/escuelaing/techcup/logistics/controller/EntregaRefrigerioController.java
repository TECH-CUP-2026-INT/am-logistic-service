package co.edu.escuelaing.techcup.logistics.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarEntregaRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.EntregaRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.logistics.security.RequireOrganizador;
import co.edu.escuelaing.techcup.logistics.service.EntregaRefrigerioService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Requerimiento 2: registro de entrega real de refrigerios por equipo o jugador.
 */
@RestController
@RequestMapping("/api/refrigerios/entregas")
@RequiredArgsConstructor
public class EntregaRefrigerioController {

    private final EntregaRefrigerioService service;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @RequireOrganizador
    @Operation(
            summary = "Registra la entrega real de un refrigerio a un equipo o jugador",
            description = "Requiere rol organizador. La identidad y el rol se extraen del JWT "
                    + "(header Authorization: Bearer) ya validado por el API Gateway; el "
                    + "interceptor de seguridad rechaza la solicitud si el rol no es organizador."
    )
    public ResponseEntity<EntregaRefrigerioResponse> registrar(
            @Valid @RequestBody RegistrarEntregaRefrigerioRequest request) {
        UUID responsableId = currentUserProvider.getCurrentUserId();
        EntregaRefrigerioResponse response = service.registrar(request, responsableId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EntregaRefrigerioResponse>> listarPorPartido(
            @RequestParam UUID partidoId) {
        return ResponseEntity.ok(service.listarPorPartido(partidoId));
    }
}
