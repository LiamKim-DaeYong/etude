create table product (
    id             uuid           primary key,
    name           varchar(200)   not null,
    price_amount   numeric(19, 4) not null check (price_amount >= 0),
    price_currency varchar(3)     not null,
    sales_status   varchar(20)    not null
);
