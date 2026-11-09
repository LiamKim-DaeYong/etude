package com.etude.order

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/** 주문은 재고에 의존하지 않는다(ADR-0004). 재고가 쓰는 예약 포트와 이벤트를 주문이 소유한다. */
@PackageInfo
@ApplicationModule(allowedDependencies = ["catalog", "shared", "platform"])
class ModuleMetadata
