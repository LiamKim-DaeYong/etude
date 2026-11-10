-- 애플리케이션이 접속하는 데이터베이스 역할(ADR-0006 2.3).
-- 테이블 소유자가 아니고 BYPASSRLS가 없어 행 수준 보안(RLS)을 받는다. 테이블 권한은 각 모듈의 마이그레이션이 준다.
-- 로컬(compose.yaml)과 테스트(Testcontainers)에서 새 데이터베이스를 만들 때 소유자 역할로 한 번 실행한다.
-- 운영에서는 같은 이름의 역할을 따로 만들고 비밀번호는 비밀 저장소로 관리한다.
create role etude_app login password 'etude_app' nosuperuser nocreatedb nocreaterole nobypassrls;
