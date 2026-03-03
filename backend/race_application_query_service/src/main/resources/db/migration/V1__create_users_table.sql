CREATE TABLE IF NOT EXISTS users
(
    id         UUID         NOT NULL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    email      VARCHAR(255) NOT NULL UNIQUE,
    dob        DATE,
    role       VARCHAR(50)  NOT NULL,
    password   VARCHAR(255) NOT NULL
);

