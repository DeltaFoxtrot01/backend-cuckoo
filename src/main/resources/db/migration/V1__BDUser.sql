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

CREATE TABLE cuckoo.hashes (
  medic_id uuid,
  hash_id SERIAL NOT NULL,
  hash_value VARCHAR(10000) NOT NULL,
  note VARCHAR(100) NOT NULL,
  is_positive BOOLEAN DEFAULT false,
  expiration_date BIGINT NOT NULL,
  medic_date BIGINT,
  PRIMARY KEY(hash_id),
  FOREIGN KEY(medic_id) REFERENCES cuckoo.users(id)

);
-- password: aPassword
INSERT INTO cuckoo.users (email,first_name,last_name, pass) VALUES ('david.dm2008@gmail.com','David','Martins','$2y$12$KSD3ItIkt1wUhjnQG662Ruaok.2J/wA9x3Buk9cl4hLa73TsMyV1. ');