package dev.etude.catalog

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

@PackageInfo
@ApplicationModule(allowedDependencies = ["shared", "platform"])
class ModuleMetadata
