CREATE TABLE loans (
    id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id                 BIGINT        NOT NULL REFERENCES books(id),
    borrower_name           VARCHAR(255)  NOT NULL,
    borrower_email          VARCHAR(255)  NOT NULL,
    loan_date               DATE          NOT NULL,
    due_date                DATE          NOT NULL,
    return_date             DATE,
    reminder_sent_at        TIMESTAMP,
    overdue_notice_sent_at  TIMESTAMP,
    created_at              TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_loans_book_id ON loans (book_id);
CREATE INDEX idx_loans_borrower_email ON loans (borrower_email);
CREATE INDEX idx_loans_active_due_date ON loans (due_date) WHERE return_date IS NULL;
CREATE INDEX idx_loans_return_date ON loans (return_date) WHERE return_date IS NOT NULL;
