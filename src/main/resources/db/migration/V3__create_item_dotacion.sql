CREATE TABLE item_dotacion (
    id                       UUID PRIMARY KEY,
    equipo_id                UUID NOT NULL,
    tipo_item                VARCHAR(20) NOT NULL,
    cantidad                 INTEGER NOT NULL,
    estado                   VARCHAR(20) NOT NULL,
    responsable_asignado_id  UUID NOT NULL,
    entregado_por_id         UUID,
    fecha_registro           TIMESTAMP NOT NULL,
    fecha_entrega            TIMESTAMP,
    observaciones            VARCHAR(500)
);

CREATE INDEX idx_item_dotacion_equipo ON item_dotacion (equipo_id);
CREATE INDEX idx_item_dotacion_estado ON item_dotacion (estado);
