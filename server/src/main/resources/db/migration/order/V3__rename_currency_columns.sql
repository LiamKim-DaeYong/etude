alter table orders rename column currency_code to total_currency;

alter table order_line rename column currency_code to unit_price_currency;
