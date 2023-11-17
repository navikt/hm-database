plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    `maven-publish`
}

group = "no.nav.hjelpemidler"
version = System.getenv("GITHUB_REF_NAME") ?: "local"

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)

    // Logging
    implementation(libs.slf4j.api)

    // Database
    api(libs.kotliquery)
    api(libs.hikaricp)
    api(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgresql)
    implementation(libs.postgresql)

    // Jackson
    implementation(libs.jackson.module.kotlin)

    // Testing
    testImplementation(libs.bundles.test)
    testRuntimeOnly(libs.testcontainers.postgresql)
    testRuntimeOnly(libs.slf4j.simple)
}

val jdkVersion = JavaLanguageVersion.of(17)
java {
    toolchain { languageVersion.set(jdkVersion) }
    withSourcesJar()
}
kotlin {
    jvmToolchain { languageVersion.set(jdkVersion) }
}

tasks.test { useJUnitPlatform() }

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/hm-database")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
