--Dentro de la conexión de postgres
DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'bootcamp_user') THEN
        CREATE ROLE bootcamp_user LOGIN PASSWORD 'vaca1234';
    END IF;
END
$$;

CREATE DATABASE onclass_bootcamp OWNER bootcamp_user;
