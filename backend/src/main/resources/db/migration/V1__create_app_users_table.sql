CREATE TABLE app_users (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name          VARCHAR(150)  NOT NULL,
    email         VARCHAR(255)  NOT NULL,
    password      VARCHAR(255)  NOT NULL,
    role          VARCHAR(20)   NOT NULL,
    blocked       BOOLEAN       NOT NULL DEFAULT FALSE,
    blocked_until TIMESTAMP,
    created_at    TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT uq_app_users_email UNIQUE (email),
    CONSTRAINT chk_app_users_role CHECK (role IN ('ADMIN', 'BIBLIOTECARIO'))
);
