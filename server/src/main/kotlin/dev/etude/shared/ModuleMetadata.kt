package dev.etude.shared

import org.springframework.modulith.ApplicationModule
import org.springframework.modulith.PackageInfo

/** Value types and the persistence base used by every module; depends on no other module. */
@PackageInfo
@ApplicationModule(allowedDependencies = [])
class ModuleMetadata
