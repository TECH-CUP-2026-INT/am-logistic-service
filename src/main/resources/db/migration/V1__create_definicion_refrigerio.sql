CREATE TABLE definicion_refrigerio (
    id              UUID PRIMARY KEY,
    partido_id      UUID NOT NULL,
    equipo_id       UUID NOT NULL,
    observaciones   VARCHAR(500),
    creado_por_id   UUID NOT NULL,
    fecha_creacion  TIMESTAMP NOT NULL,
    CONSTRAINT uq_definicion_refrigerio_partido_equipo UNIQUE (partido_id, equipo_id)
);

CREATE TABLE item_refrigerio_definido (
    definicion_refrigerio_id UUID NOT NULL REFERENCES definicion_refrigerio (id) ON DELETE CASCADE,
    descripcion               VARCHAR(200) NOT NULL,
    cantidad                  INTEGER NOT NULL
);

CREATE INDEX idx_item_refrigerio_definido_definicion ON item_refrigerio_definido (definicion_refrigerio_id);
