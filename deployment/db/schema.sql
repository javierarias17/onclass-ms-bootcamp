--Dentro de la nueva conexión de onclass_bootcamp

CREATE SCHEMA IF NOT EXISTS onclass_bootcamp AUTHORIZATION bootcamp_user;

CREATE TABLE IF NOT EXISTS onclass_bootcamp.bootcamps (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(50) NOT NULL,
    description       VARCHAR(90) NOT NULL,
    launch_date       DATE NOT NULL,
    duration_in_weeks INTEGER NOT NULL,
    status            VARCHAR(10) NOT NULL DEFAULT 'CREATING',
    capability_count  INTEGER NOT NULL DEFAULT 0,
    version           BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS bootcamps_name_lower_unique_idx
    ON onclass_bootcamp.bootcamps (LOWER(name));
