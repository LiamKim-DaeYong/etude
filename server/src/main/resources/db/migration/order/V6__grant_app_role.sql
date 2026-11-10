-- 애플리케이션 역할(etude_app)에 이 모듈의 테이블 권한을 준다(ADR-0006 2.3). 역할은 db/init에서 만든다.
-- 회사로 나누기 전 테이블이라 지금의 동작(조회·저장·수정·삭제)을 그대로 허용한다.
grant select, insert, update, delete on orders, order_line to etude_app;
