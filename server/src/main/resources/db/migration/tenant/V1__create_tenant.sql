create table tenant (
    id         uuid         primary key,
    name       varchar(200) not null,
    created_at timestamptz  not null,
    updated_at timestamptz  not null
);
