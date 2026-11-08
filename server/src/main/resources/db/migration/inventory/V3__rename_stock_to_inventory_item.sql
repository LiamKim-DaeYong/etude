alter table stock rename to inventory_item;

alter table inventory_item rename column quantity to available_quantity;

alter table inventory_item rename constraint stock_pkey to inventory_item_pkey;
alter table inventory_item rename constraint stock_quantity_check to inventory_item_available_quantity_check;
alter table inventory_item rename constraint uk_stock_product_id to uk_inventory_item_product_id;
