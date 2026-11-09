create table users (
    id            uuid         primary key,
    tenant_id     uuid         not null,
    role          varchar(20)  not null,
    username      varchar(50)  not null,
    email         varchar(254) unique,
    password_hash varchar(255) not null,
    created_at    timestamptz  not null,
    updated_at    timestamptz  not null,
    unique (tenant_id, username),
    check (role <> 'OWNER' or email is not null)
);
