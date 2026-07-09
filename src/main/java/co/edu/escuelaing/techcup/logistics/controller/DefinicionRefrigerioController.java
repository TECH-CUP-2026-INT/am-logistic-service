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

import co.edu.escuelaing.techcup.logistics.dto.request.CrearDefinicionRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.DefinicionRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.security.CurrentUser;
import co.edu.escuelaing.techcup.logistics.security.RequestHeaders;
import co.edu.escuelaing.techcup.logistics.security.RequireOrganizador;
import co.edu.escuelaing.techcup.logistics.service.DefinicionRefrigerioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Requerimiento 1: definicion de refrigerios por equipo/partido a cargo del organizador.
 */
@RestController
@RequestMapping("/api/refrigerios/definiciones")
@RequiredArgsConstructor
public class DefinicionRefrigerioController {

    private final DefinicionRefrigerioService service;

    @PostMapping
    @RequireOrganizador
    @Operation(
            summary = "Define refrigerio(s) para un equipo en un partido",
            description = "Requiere rol organizador. El header X-User-Role no viaja como parametro "
                    + "del metodo: lo valida el interceptor de seguridad antes de llegar aqui.",
            parameters = @Parameter(name = RequestHeaders.X_USER_ROLE, in = ParameterIn.HEADER,
                    required = true, example = RequestHeaders.ROLE_ORGANIZADOR)
    )
    public ResponseEntity<DefinicionRefrigerioResponse> crear(
            @Valid @RequestBody CrearDefinicionRefrigerioRequest request,
            @RequestHeader(RequestHeaders.X_USER_ID) String userId) {
        UUID creadoPorId = CurrentUser.idFrom(userId);
        DefinicionRefrigerioResponse response = service.crear(request, creadoPorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<DefinicionRefrigerioResponse>> listarPorPartido(
            @RequestParam UUID partidoId) {
        return ResponseEntity.ok(service.listarPorPartido(partidoId));
    }
}
