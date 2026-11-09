package com.etude.identity

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

@PackageInfo
@ApplicationModule(allowedDependencies = ["tenant", "shared", "platform"])
class ModuleMetadata
