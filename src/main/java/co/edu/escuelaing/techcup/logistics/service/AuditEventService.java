package co.edu.escuelaing.techcup.logistics.service;

import java.util.List;

import co.edu.escuelaing.techcup.logistics.dto.response.AuditEventResponse;

public interface AuditEventService {

    List<AuditEventResponse> listarEventos();
}
