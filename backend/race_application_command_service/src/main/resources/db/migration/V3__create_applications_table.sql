CREATE TABLE IF NOT EXISTS applications
(
    id         UUID         NOT NULL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    club       VARCHAR(255),
    race_id    UUID         NOT NULL,
    user_id    UUID,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now()
);

