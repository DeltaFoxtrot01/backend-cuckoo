CREATE SCHEMA IF NOT EXISTS cuckoo;

CREATE TABLE cuckoo.users (
    email VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    pass VARCHAR(60) NOT NULL,
    CONSTRAINT proper_email CHECK (email ~* '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$'),
    PRIMARY KEY(email)
);

INSERT INTO cuckoo.users (email,first_name,last_name, pass) VALUES ('david.dm2008@gmail.com','David','Martins','$2y$12$PR1ulfwPgIHJhn7svCM4kO8bpeumIjOa1pg4sRNg4aTxMr11fP0AG ');