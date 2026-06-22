DROP TABLE IF EXISTS audit_entity;
CREATE TABLE audit_entity
(
    id          BIGINT      NOT NULL PRIMARY KEY,
    name        VARCHAR(64) NOT NULL,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_user BIGINT,
    update_user BIGINT
);
