alter table product
    add column created_at timestamptz not null default now(),
    add column updated_at timestamptz not null default now();

alter table product
    alter column created_at drop default,
    alter column updated_at drop default;
