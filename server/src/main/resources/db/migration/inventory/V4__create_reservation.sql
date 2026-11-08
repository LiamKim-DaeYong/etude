create table reservation (
    id         uuid        primary key,
    order_id   uuid        not null,
    product_id uuid        not null,
    quantity   integer     not null check (quantity > 0),
    status     varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_reservation_order_id_product_id unique (order_id, product_id)
);
