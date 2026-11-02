package dev.etude

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class ModularityTests {

    private val modules = ApplicationModules.of(EtudeApplication::class.java)

    @Test
    fun verifiesModularStructure() {
        modules.verify()
    }

    @Test
    fun writesDocumentation() {
        Documenter(modules).writeDocumentation()
    }
}
