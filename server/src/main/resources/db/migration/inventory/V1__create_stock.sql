create table stock (
    id         uuid    primary key,
    product_id uuid    not null,
    quantity   integer not null check (quantity >= 0),
    constraint uk_stock_product_id unique (product_id)
);
