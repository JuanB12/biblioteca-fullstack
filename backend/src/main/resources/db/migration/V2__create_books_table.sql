CREATE TABLE books (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title             VARCHAR(500)  NOT NULL,
    author            VARCHAR(255)  NOT NULL,
    isbn              VARCHAR(20)   NOT NULL,
    publication_year  INTEGER,
    status            VARCHAR(20)   NOT NULL DEFAULT 'DISPONIBLE',
    cover_url         VARCHAR(1000),
    subjects          TEXT,
    version           BIGINT        NOT NULL DEFAULT 0,
    created_at        TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uq_books_isbn UNIQUE (isbn),
    CONSTRAINT chk_books_status CHECK (status IN ('DISPONIBLE', 'PRESTADO', 'RESERVADO'))
);
