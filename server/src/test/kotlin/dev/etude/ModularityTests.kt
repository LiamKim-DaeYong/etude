package dev.etude

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules

class ModularityTests {

    private val modules = ApplicationModules.of(EtudeApplication::class.java)

    @Test
    fun verifiesModularStructure() {
        modules.verify()
    }

}
