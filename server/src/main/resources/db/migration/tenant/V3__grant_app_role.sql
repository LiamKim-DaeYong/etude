-- 애플리케이션 역할(etude_app)에 이 모듈의 테이블 권한을 준다(ADR-0006 2.3). 역할은 db/init에서 만든다.
-- 회사는 테넌트 데이터가 아니다. 삭제하는 기능이 없어 삭제 권한은 주지 않는다.
grant select, insert, update on tenant to etude_app;
