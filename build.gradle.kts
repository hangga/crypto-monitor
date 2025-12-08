import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"   // <--- WAJIB
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "id.web.hangga"
version = "1.0-SNAPSHOT"

val ktorVersion = "3.3.3"
val resilience4jVersion = "2.3.0"
val coroutinesVersion = "1.9.0" // versi terbaru & aman

repositories {
    mavenCentral()
}

dependencies {

    // -------------------------
    // KTOR SERVER
    // -------------------------
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // -------------------------
    // KTOR CLIENT
    // -------------------------
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // -------------------------
    // COROUTINES (terbaru)
    // -------------------------
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")

    // -------------------------
    // RESILIENCE4J
    // -------------------------
    implementation("io.github.resilience4j:resilience4j-core:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")

    // -------------------------
    // LOGGING
    // -------------------------
    implementation("ch.qos.logback:logback-classic:1.5.21")

    // -------------------------
    // TESTING
    // -------------------------
    testImplementation(kotlin("test"))

    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("id.web.hangga.ApplicationKt")
}

tasks {
    shadowJar {
        archiveBaseName.set("crypto-monitor")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/main/resources")
        }
    }
}