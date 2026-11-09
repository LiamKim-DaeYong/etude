plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.ktlint)
}

group = "com.etude"
version = "0.0.1-SNAPSHOT"
description = "etude"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

// Mockito's inline mock maker needs its agent at JVM start; loading it dynamically is deprecated in recent JDKs.
val mockitoAgent: Configuration by configurations.creating

dependencies {
    implementation(platform(libs.spring.modulith.bom))

    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.flyway)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.modulith.starter.core)
    implementation(libs.spring.modulith.starter.jdbc)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.module.kotlin)
    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.spring.boot.docker.compose)
    runtimeOnly(libs.spring.modulith.runtime)
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.flyway.database.postgresql)

    testImplementation(libs.spring.boot.starter.test.actuator)
    testImplementation(libs.spring.boot.starter.test.data.jpa)
    testImplementation(libs.spring.boot.starter.test.validation)
    testImplementation(libs.spring.boot.starter.test.webmvc)
    testImplementation(libs.spring.boot.starter.test.security)
    testImplementation(libs.spring.modulith.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.json.schema.validator)
    testRuntimeOnly(libs.junit.platform.launcher)
    mockitoAgent(libs.mockito.core) { isTransitive = false }
}

kotlin {
    compilerOptions {
        // -Xemit-jvm-type-annotations: keep type-use annotations such as List<@Valid Line> for Bean Validation
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property", "-Xemit-jvm-type-annotations")
    }
}

ktlint {
    version.set(libs.versions.ktlint.get())
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.bootRun {
    systemProperty("spring.profiles.active", "local")
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}
