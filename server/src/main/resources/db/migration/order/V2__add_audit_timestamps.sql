alter table orders
    add column created_at timestamptz not null default now(),
    add column updated_at timestamptz not null default now();

alter table orders
    alter column created_at drop default,
    alter column updated_at drop default;

alter table order_line
    add column created_at timestamptz not null default now(),
    add column updated_at timestamptz not null default now();

alter table order_line
    alter column created_at drop default,
    alter column updated_at drop default;
