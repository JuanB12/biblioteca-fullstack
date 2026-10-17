CREATE TABLE reservations (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id          BIGINT        NOT NULL REFERENCES books(id),
    requester_email  VARCHAR(255)  NOT NULL,
    request_date     TIMESTAMP     NOT NULL DEFAULT now(),
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    created_at       TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT chk_reservations_status CHECK (status IN ('PENDIENTE', 'NOTIFICADO', 'CANCELADO', 'CUMPLIDO'))
);

CREATE INDEX idx_reservations_book_status_date ON reservations (book_id, status, request_date);
