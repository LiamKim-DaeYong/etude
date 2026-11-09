package com.etude.order

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/** Order never depends on inventory (ADR-0004): it owns the reservation port and the events that inventory uses. */
@PackageInfo
@ApplicationModule(allowedDependencies = ["catalog", "shared", "platform"])
class ModuleMetadata
