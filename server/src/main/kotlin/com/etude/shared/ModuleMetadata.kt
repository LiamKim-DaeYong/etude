package com.etude.shared

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/** 모든 모듈이 쓰는 값 타입과 영속성 기반. 다른 모듈에 의존하지 않는다. */
@PackageInfo
@ApplicationModule(allowedDependencies = [])
class ModuleMetadata
