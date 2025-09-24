val intellijVersion = "2025.2.1"

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.9.0"
    id("org.jetbrains.compose") version "1.9.0-beta03"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
}

group = "com.plusmobileapps"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
    google()
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    maven("https://www.jetbrains.com/intellij-repository/releases")
}

// Configure IntelliJ Platform Gradle Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        create("IC", intellijVersion)
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Jewel bundled modules for IntelliJ Platform
        bundledModule("intellij.platform.jewel.foundation")
        bundledModule("intellij.platform.jewel.ui")
        bundledModule("intellij.platform.jewel.ideLafBridge")
        bundledModule("intellij.platform.jewel.markdown.core")
        bundledModule("intellij.platform.jewel.markdown.ideLafBridgeStyling")
        bundledModule("intellij.libraries.compose.foundation.desktop")
        bundledModule("intellij.libraries.skiko")
    }

    // Compose runtime dependencies for Jewel
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.ui)

//    implementation("org.jetbrains.jewel:foundation:0.27.0")

    // Do not bring in Material (we use Jewel)
    implementation(compose.desktop.currentOs) {
        exclude(group = "org.jetbrains.compose.material")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
        }

        changeNotes = """
            Initial version
        """.trimIndent()
    }
    publishing {
        token = providers.gradleProperty("intellijPlatformPublishingToken")
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
