import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm") version "2.2.20"
}

group = "id.web.hangga"
version = "1.0-SNAPSHOT"

val ktorVersion = "3.3.3"
val resilience4jVersion = "2.3.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    // Resilience4j
    implementation("io.github.resilience4j:resilience4j-core:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")

    // HTTP client for calling external API
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.21")

//    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
