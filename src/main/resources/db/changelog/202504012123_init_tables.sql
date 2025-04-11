CREATE SCHEMA tt;

CREATE TABLE tt.status
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL
);

INSERT INTO tt.status (id, name)
VALUES (1, 'TO_DO');
INSERT INTO tt.status (id, name)
VALUES (2, 'IN_PROGRESS');
INSERT INTO tt.status (id, name)
VALUES (3, 'DONE');
INSERT INTO tt.status (id, name)
VALUES (4, 'DELETED');

CREATE TABLE tt.task
(
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(64) NOT NULL,
    description TEXT        NOT NULL,
    status      INT REFERENCES tt.status (id),
    dead_line   DATE        NOT NULL,
    assignee    INT         NOT NULL,
    author      INT         NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);