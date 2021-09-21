CREATE USER uservaccinationinventory WITH PASSWORD 'uservaccinationinventory123' CREATEDB;
CREATE DATABASE uservaccinationinventory
    WITH
    OWNER = uservaccinationinventory
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;
CREATE USER keycloak WITH PASSWORD 'keycloak123' CREATEDB;
CREATE DATABASE keycloak
    WITH
    OWNER = keycloak
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;
