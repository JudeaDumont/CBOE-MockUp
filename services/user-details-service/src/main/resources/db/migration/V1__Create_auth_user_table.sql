CREATE TABLE auth_user
(
    id       BIGSERIAL PRIMARY KEY,
    email    VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(50)  NOT NULL
);