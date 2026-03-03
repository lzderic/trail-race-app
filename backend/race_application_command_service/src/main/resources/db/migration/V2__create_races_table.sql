CREATE TABLE IF NOT EXISTS races
(
    id       UUID         NOT NULL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    distance VARCHAR(50)  NOT NULL
);

