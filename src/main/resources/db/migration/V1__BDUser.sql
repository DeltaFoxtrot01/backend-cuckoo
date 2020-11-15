CREATE SCHEMA IF NOT EXISTS cuckoo;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE cuckoo.users (
    id uuid DEFAULT uuid_generate_v4 (),
    email VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    pass VARCHAR(60) NOT NULL,
    CONSTRAINT proper_email CHECK (email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'),
    UNIQUE(email),
    PRIMARY KEY(id)
);
-- password: aPassword
INSERT INTO cuckoo.users (email,first_name,last_name, pass) VALUES ('david.dm2008@gmail.com','David','Martins','$2y$12$KSD3ItIkt1wUhjnQG662Ruaok.2J/wA9x3Buk9cl4hLa73TsMyV1. ');