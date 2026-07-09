package co.edu.escuelaing.techcup.logistics.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.escuelaing.techcup.logistics.dto.request.RegistrarEntregaRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.EntregaRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.security.CurrentUser;
import co.edu.escuelaing.techcup.logistics.security.RequestHeaders;
import co.edu.escuelaing.techcup.logistics.security.RequireOrganizador;
import co.edu.escuelaing.techcup.logistics.service.EntregaRefrigerioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

    @PostMapping
    @RequireOrganizador
    @Operation(
            summary = "Registra la entrega real de un refrigerio a un equipo o jugador",
            description = "Requiere rol organizador. El header X-User-Role no viaja como parametro "
                    + "del metodo: lo valida el interceptor de seguridad antes de llegar aqui.",
            parameters = @Parameter(name = RequestHeaders.X_USER_ROLE, in = ParameterIn.HEADER,
                    required = true, example = RequestHeaders.ROLE_ORGANIZADOR)
    )
    public ResponseEntity<EntregaRefrigerioResponse> registrar(
            @Valid @RequestBody RegistrarEntregaRefrigerioRequest request,
            @RequestHeader(RequestHeaders.X_USER_ID) String userId) {
        UUID responsableId = CurrentUser.idFrom(userId);
        EntregaRefrigerioResponse response = service.registrar(request, responsableId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EntregaRefrigerioResponse>> listarPorPartido(
            @RequestParam UUID partidoId) {
        return ResponseEntity.ok(service.listarPorPartido(partidoId));
    }
}
