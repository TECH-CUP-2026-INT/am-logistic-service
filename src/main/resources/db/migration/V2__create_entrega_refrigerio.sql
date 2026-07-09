CREATE TABLE entrega_refrigerio (
    id                        UUID PRIMARY KEY,
    definicion_refrigerio_id  UUID NOT NULL REFERENCES definicion_refrigerio (id),
    partido_id                UUID NOT NULL,
    tipo_destinatario         VARCHAR(20) NOT NULL,
    destinatario_id           UUID NOT NULL,
    responsable_id            UUID NOT NULL,
    fecha_entrega             TIMESTAMP NOT NULL,
    observaciones             VARCHAR(500),
    CONSTRAINT uq_entrega_refrigerio_destinatario UNIQUE (partido_id, tipo_destinatario, destinatario_id)
);

CREATE INDEX idx_entrega_refrigerio_definicion ON entrega_refrigerio (definicion_refrigerio_id);
