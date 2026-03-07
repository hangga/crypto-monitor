import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"   // <--- WAJIB
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "id.web.hangga"
version = "1.0-SNAPSHOT"

val resilience4jVersion = "2.3.0"
val coroutinesVersion = "1.9.0" // versi terbaru & aman

repositories {
    mavenCentral()
}

dependencies {

    implementation(platform("io.ktor:ktor-bom:3.4.0"))

    // -------------------------
    // KTOR SERVER
    // -------------------------
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-cio")
    implementation("io.ktor:ktor-server-content-negotiation")

    // -------------------------
    // KTOR CLIENT
    // -------------------------
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")
    implementation("io.ktor:ktor-client-content-negotiation")

    // -------------------------
    // COROUTINES (latest)
    // -------------------------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // -------------------------
    // RESILIENCE4J
    // -------------------------
    implementation("io.github.resilience4j:resilience4j-core:$resilience4jVersion") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")

    // -------------------------
    // LOGGING
    // -------------------------
    implementation("ch.qos.logback:logback-classic:1.5.25")

    // -------------------------
    // TESTING
    // -------------------------
    testImplementation(kotlin("test"))

    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xno-param-assertions")
        freeCompilerArgs.add("-Xno-call-assertions")
    }
}

application {
    mainClass.set("id.web.hangga.ApplicationKt")
}

tasks.shadowJar {
    archiveBaseName.set("crypto-monitor")
    archiveClassifier.set("")
    archiveVersion.set("")

    minimize {
        exclude(dependency("io.ktor:.*"))
        exclude(dependency("org.jetbrains.kotlinx:.*"))
        exclude(dependency("ch.qos.logback:.*"))
    }

    mergeServiceFiles()
}

tasks.withType<Jar> {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}

configurations.all {
    exclude(group = "org.fusesource.jansi")
}