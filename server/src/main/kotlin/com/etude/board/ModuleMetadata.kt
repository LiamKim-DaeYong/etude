package com.etude.board

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/**
 * 회사 안의 게시판과 게시글(ADR-0008). 서비스 공통 층이다.
 * 회사 등록 이벤트로 기본 게시판을 준비하고(`tenant`), 작성자의 아이디를 조회한다(`identity`).
 */
@PackageInfo
@ApplicationModule(allowedDependencies = ["tenant", "identity", "shared", "platform"])
class ModuleMetadata
