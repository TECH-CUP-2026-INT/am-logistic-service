package co.edu.escuelaing.techcup.logistics.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.escuelaing.techcup.logistics.dto.response.AuditEventResponse;
import co.edu.escuelaing.techcup.logistics.service.AuditEventService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

/**
 * Requerimiento 4: audit log local de eventos ya ocurridos en Logistica
 * (definiciones, entregas y dotacion), accesible solo por Admin y Organizador.
 */
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditEventController {

    private final AuditEventService service;

    @GetMapping("/eventos")
    @PreAuthorize("hasAnyRole('ADMIN','ORGANIZADOR')")
    @Operation(
            summary = "Lista los eventos de dominio ya ocurridos en Logistica",
            description = "Requiere rol ADMIN u ORGANIZADOR. Agrega, en memoria y ordenado "
                    + "descendentemente por fecha, los eventos ya registrados sobre definiciones de "
                    + "refrigerio, entregas de refrigerio e items de dotacion (registro, entrega y "
                    + "devolucion). No crea ningun registro nuevo: es una vista de solo lectura."
    )
    public ResponseEntity<List<AuditEventResponse>> listarEventos() {
        return ResponseEntity.ok(service.listarEventos());
    }
}
