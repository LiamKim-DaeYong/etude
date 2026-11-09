package com.etude.inventory

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

@PackageInfo
@ApplicationModule(allowedDependencies = ["catalog", "order", "shared", "platform"])
class ModuleMetadata
