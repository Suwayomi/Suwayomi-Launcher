import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jmailen.gradle.kotlinter.tasks.LintTask
import org.jmailen.gradle.kotlinter.tasks.FormatTask

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinter)
    application
    alias(libs.plugins.shadowjar)
}

buildscript {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }

    dependencies {
        // ktlint rules
        classpath("com.github.Suwayomi:Tachidesk-ktlintRules:a8196206b4")
    }
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
    implementation(libs.miglayout)

    // Config
    implementation(libs.config)
    implementation(libs.config4k)

    // Test
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.coroutines.test)
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<KotlinJvmCompile> {
        dependsOn("formatKotlin")
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }

    withType<LintTask> {
        source(files("src/kotlin"))
    }

    withType<FormatTask> {
        source(files("src/kotlin"))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
}

application {
    mainClass.set("suwayomi.tachidesk.launcher.MainKt")
}