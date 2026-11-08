alter table product rename column sales_status to status;

update product set status = 'ACTIVE' where status = 'ON_SALE';
