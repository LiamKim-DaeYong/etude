alter table orders rename column ordered_at to placed_at;

alter table order_line rename column line_no to line_number;
alter table order_line rename constraint uk_order_line_order_line_no to uk_order_line_order_line_number;

update order_line set status = 'PLACED' where status = 'ORDERED';
