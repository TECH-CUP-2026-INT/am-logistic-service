package co.edu.escuelaing.techcup.logistics.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.escuelaing.techcup.logistics.dto.request.CrearDefinicionRefrigerioRequest;
import co.edu.escuelaing.techcup.logistics.dto.response.DefinicionRefrigerioResponse;
import co.edu.escuelaing.techcup.logistics.security.CurrentUserProvider;
import co.edu.escuelaing.techcup.logistics.security.RequireOrganizador;
import co.edu.escuelaing.techcup.logistics.service.DefinicionRefrigerioService;
import io.swagger.v3.oas.annotations.Operation;
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
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @RequireOrganizador
    @Operation(
            summary = "Define refrigerio(s) para un equipo en un partido",
            description = "Requiere rol organizador. Solo se permite para equipos que ya "
                    + "clasificaron a segunda fase segun los resultados del Servicio de Torneos "
                    + "(identificado automaticamente, sin intervencion manual). La identidad y el "
                    + "rol se extraen del JWT (header Authorization: Bearer) ya validado por el API "
                    + "Gateway; el interceptor de seguridad rechaza la solicitud si el rol no es "
                    + "organizador."
    )
    public ResponseEntity<DefinicionRefrigerioResponse> crear(
            @Valid @RequestBody CrearDefinicionRefrigerioRequest request) {
        UUID creadoPorId = currentUserProvider.getCurrentUserId();
        DefinicionRefrigerioResponse response = service.crear(request, creadoPorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZADOR')")
    @Operation(summary = "Lista las definiciones de refrigerio de un partido", description = "Requiere rol ADMIN u ORGANIZADOR.")
    public ResponseEntity<List<DefinicionRefrigerioResponse>> listarPorPartido(
            @RequestParam UUID partidoId) {
        return ResponseEntity.ok(service.listarPorPartido(partidoId));
    }
}
