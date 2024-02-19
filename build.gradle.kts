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
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(libs.bundles.coroutines)
    implementation(libs.settings.core)
    implementation(libs.appdirs)
    implementation(libs.okhttp)

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

val MainClass = "suwayomi.tachidesk.launcher.MainKt"
application {
    mainClass.set(MainClass)
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<KotlinJvmCompile> {
        dependsOn("formatKotlin")
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_1_8.toString()
            freeCompilerArgs = listOf("-Xcontext-receivers")
        }
    }

    withType<LintTask> {
        source(files("src/kotlin"))
    }

    withType<FormatTask> {
        source(files("src/kotlin"))
    }

    shadowJar {
        manifest {
            attributes(
                "Main-Class" to MainClass,
                "Implementation-Title" to rootProject.name,
                "Implementation-Vendor" to "The Suwayomi Project",
                "Specification-Version" to project.version.toString(),
            )
        }
        archiveBaseName.set(rootProject.name)
        archiveClassifier.set(null as String?)
        destinationDirectory.set(File("$rootDir/build"))
    }
}
