package com.plusmobileapps.devkit.ui

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ModuleCreator(
    private val project: Project,
    private val parentDirectory: VirtualFile,
    private val namespace: String,
    private val directoryName: String,
    private val createPublic: Boolean,
    private val createImpl: Boolean,
    private val createTesting: Boolean
) {
    companion object {
        private const val PUBLIC_MODULE_NAME = "public"
        private const val IMPL_MODULE_NAME = "impl"
        private const val TESTING_MODULE_NAME = "testing"
    }

    fun createModules() {
        try {
            WriteAction.runAndWait<Exception> {
                // Create main directory
                val mainDir = parentDirectory.createChildDirectory(this, directoryName)

                // Create modules based on selected options
                if (createPublic) {
                    createModuleDirectory(mainDir, PUBLIC_MODULE_NAME)
                }
                if (createImpl) {
                    createModuleDirectory(mainDir, IMPL_MODULE_NAME)
                }
                if (createTesting) {
                    createModuleDirectory(mainDir, TESTING_MODULE_NAME)
                }
            }

            showNotification("Modules created successfully", NotificationType.INFORMATION)
        } catch (e: Exception) {
            showNotification("Failed to create modules: ${e.message}", NotificationType.ERROR)
        }
    }

    private fun createModuleDirectory(parent: VirtualFile, moduleName: String) {
        val moduleDir = parent.createChildDirectory(this, moduleName)

        // Create standard Android/Java directory structure
        val srcDir = moduleDir.createChildDirectory(this, "src")
        val mainDir = srcDir.createChildDirectory(this, "main")

        // Create Java/Kotlin source directories with namespace
        val javaDir = mainDir.createChildDirectory(this, "java")
        createNamespaceDirectories(javaDir, namespace)

        // Create Android resources directory if needed
        mainDir.createChildDirectory(this, "res")

        // Create basic Android module files like AndroidManifest.xml
        if (moduleName != TESTING_MODULE_NAME) {
            val manifestContent = """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android"
                    package="$namespace.$directoryName.$moduleName">
                </manifest>
            """.trimIndent()

            val manifestFile = mainDir.createChildData(this, "AndroidManifest.xml")
            manifestFile.setBinaryContent(manifestContent.toByteArray())
        }

        // Create build.gradle file
        val buildGradleContent = when (moduleName) {
            PUBLIC_MODULE_NAME -> createPublicBuildGradle()
            IMPL_MODULE_NAME -> createImplBuildGradle()
            TESTING_MODULE_NAME -> createTestingBuildGradle()
            else -> ""
        }

        val buildGradleFile = moduleDir.createChildData(this, "build.gradle.kts")
        buildGradleFile.setBinaryContent(buildGradleContent.toByteArray())
    }

    private fun createNamespaceDirectories(parent: VirtualFile, namespace: String) {
        var current = parent
        namespace.split('.').forEach { part ->
            current = current.createChildDirectory(this, part)
        }

        // Also create directory with module name
        current.createChildDirectory(this, directoryName)
    }

    private fun createPublicBuildGradle(): String {
        return """
            plugins {
                id("com.android.library")
                id("org.jetbrains.kotlin.android")
            }
            
            android {
                namespace = "$namespace.$directoryName.${PUBLIC_MODULE_NAME}"
                compileSdk = 34
                
                defaultConfig {
                    minSdk = 24
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                
                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                    }
                }
                
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                
                kotlinOptions {
                    jvmTarget = "17"
                }
            }
            
            dependencies {
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("com.google.android.material:material:1.11.0")
                testImplementation("junit:junit:4.13.2")
                androidTestImplementation("androidx.test.ext:junit:1.1.5")
                androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
            }
        """.trimIndent()
    }

    private fun createImplBuildGradle(): String {
        return """
            plugins {
                id("com.android.library")
                id("org.jetbrains.kotlin.android")
            }
            
            android {
                namespace = "$namespace.$directoryName.${IMPL_MODULE_NAME}"
                compileSdk = 34
                
                defaultConfig {
                    minSdk = 24
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                
                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                    }
                }
                
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                
                kotlinOptions {
                    jvmTarget = "17"
                }
            }
            
            dependencies {
                implementation(project(":$directoryName:${PUBLIC_MODULE_NAME}"))
                
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("com.google.android.material:material:1.11.0")
                testImplementation("junit:junit:4.13.2")
                androidTestImplementation("androidx.test.ext:junit:1.1.5")
                androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
            }
        """.trimIndent()
    }

    private fun createTestingBuildGradle(): String {
        return """
            plugins {
                id("com.android.library")
                id("org.jetbrains.kotlin.android")
            }
            
            android {
                namespace = "$namespace.$directoryName.${TESTING_MODULE_NAME}"
                compileSdk = 34
                
                defaultConfig {
                    minSdk = 24
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                
                buildTypes {
                    release {
                        isMinifyEnabled = false
                        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                    }
                }
                
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
                
                kotlinOptions {
                    jvmTarget = "17"
                }
            }
            
            dependencies {
                implementation(project(":$directoryName:${PUBLIC_MODULE_NAME}"))
                implementation(project(":$directoryName:${IMPL_MODULE_NAME}"))
                
                implementation("androidx.core:core-ktx:1.12.0")
                implementation("androidx.appcompat:appcompat:1.6.1")
                implementation("com.google.android.material:material:1.11.0")
                testImplementation("junit:junit:4.13.2")
                androidTestImplementation("androidx.test.ext:junit:1.1.5")
                androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
                
                // Testing dependencies
                testImplementation("org.mockito:mockito-core:5.7.0")
                testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
                testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        """.trimIndent()
    }

    private fun showNotification(content: String, type: NotificationType) {
        ApplicationManager.getApplication().invokeLater {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Plus Dev Kit Notifications")
                .createNotification(content, type)
                .notify(project)
        }
    }
}
