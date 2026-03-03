CREATE TABLE IF NOT EXISTS applications
(
    id         UUID         NOT NULL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name  VARCHAR(255) NOT NULL,
    club       VARCHAR(255),
    race_id    UUID         NOT NULL,
    CONSTRAINT fk_applications_race FOREIGN KEY (race_id) REFERENCES races (id)
);

