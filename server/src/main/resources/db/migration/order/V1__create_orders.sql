create table orders (
    id            uuid           primary key,
    customer_id   uuid           not null,
    status        varchar(20)    not null,
    ordered_at    timestamptz    not null,
    total_amount  numeric(19, 4) not null check (total_amount >= 0),
    currency_code varchar(3)     not null
);

create table order_line (
    id                uuid           primary key,
    order_id          uuid           not null references orders (id),
    line_no           integer        not null,
    product_id        uuid           not null,
    product_name      varchar(200)   not null,
    quantity          integer        not null check (quantity > 0),
    unit_price_amount numeric(19, 4) not null check (unit_price_amount >= 0),
    currency_code     varchar(3)     not null,
    status            varchar(20)    not null,
    constraint uk_order_line_order_line_no unique (order_id, line_no)
);
