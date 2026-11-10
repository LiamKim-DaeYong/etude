-- 회사의 게시판과 게시글. 둘 다 테넌트 데이터다(ADR-0006, ADR-0008).

create table board (
    id         uuid         primary key,
    tenant_id  uuid         not null,
    -- 기본 게시판은 이름이 없고 클라이언트가 언어에 맞게 보여 준다.
    name       varchar(100),
    is_default boolean      not null,
    created_at timestamptz  not null,
    updated_at timestamptz  not null,
    -- 게시글이 같은 회사의 게시판만 가리키도록 복합 외래 키의 대상이 된다.
    unique (id, tenant_id)
);

-- 회사마다 기본 게시판은 하나다. 기본 게시판 준비를 다시 하거나 동시에 해도 하나만 남는다.
create unique index board_default_per_tenant on board (tenant_id) where is_default;

create table post (
    id         uuid         primary key,
    tenant_id  uuid         not null,
    board_id   uuid         not null,
    -- 사용자(identity 모듈)는 다른 모듈이라 외래 키를 두지 않는다(ADR-0007 2.5).
    author_id  uuid         not null,
    title      varchar(200) not null,
    body       text         not null,
    posted_at  timestamptz  not null,
    edited_at  timestamptz,
    deleted_at timestamptz,
    created_at timestamptz  not null,
    updated_at timestamptz  not null,
    -- 다른 회사의 게시판에 글이 붙지 않는다.
    foreign key (board_id, tenant_id) references board (id, tenant_id)
);

-- 게시판의 글 목록과 회사 전체의 최근 글. 삭제한 글은 빼고 최신순으로 읽는다.
create index post_board_posted_at on post (tenant_id, board_id, posted_at desc, id desc) where deleted_at is null;
create index post_posted_at on post (tenant_id, posted_at desc, id desc) where deleted_at is null;

-- 두 번째 방어선: 현재 회사(app.tenant_id)의 행만 읽고 쓴다(ADR-0006 2.3).
-- 값이 없거나 빈 문자열(트랜잭션이 끝난 연결, 플랫폼 블록)이면 어떤 행도 보이지 않고 쓸 수 없다.
alter table board enable row level security;
create policy tenant_isolation on board
    using (tenant_id = nullif(current_setting('app.tenant_id', true), '')::uuid)
    with check (tenant_id = nullif(current_setting('app.tenant_id', true), '')::uuid);

alter table post enable row level security;
create policy tenant_isolation on post
    using (tenant_id = nullif(current_setting('app.tenant_id', true), '')::uuid)
    with check (tenant_id = nullif(current_setting('app.tenant_id', true), '')::uuid);

-- 게시글 삭제는 deleted_at을 남기는 방식이라 애플리케이션 역할에 delete 권한을 주지 않는다.
grant select, insert, update on board, post to etude_app;
