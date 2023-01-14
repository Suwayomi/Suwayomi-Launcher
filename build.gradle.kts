import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinter)
    application
    alias(libs.plugins.shadowjar)
}

group = "suwayomi.tachidesk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.coroutines)
    implementation(libs.settings.core)
    implementation(libs.appdirs)

    // Logging
    implementation(libs.slf4japi)
    implementation(libs.logback)
    implementation(libs.kotlinlogging)

    // UI
    implementation(libs.darklaf)

    // Test
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.coroutines.test)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

application {
    mainClass.set("suwayomi.tachidesk.launcher.MainKt")
}