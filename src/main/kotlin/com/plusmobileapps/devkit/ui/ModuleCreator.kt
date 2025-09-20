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

        // Create multiplatform source directory structure
        val srcDir = moduleDir.createChildDirectory(this, "src")
        val commonMainDir = srcDir.createChildDirectory(this, "commonMain")
        val kotlinDir = commonMainDir.createChildDirectory(this, "kotlin")
        createNamespaceDirectories(kotlinDir, namespace)

        // Create build.gradle.kts file for multiplatform
        val buildGradleContent = createMultiplatformBuildGradle(moduleName)
        val buildGradleFile = moduleDir.createChildData(this, "build.gradle.kts")
        buildGradleFile.setBinaryContent(buildGradleContent.toByteArray())
    }

    private fun createMultiplatformBuildGradle(moduleName: String): String {
        return """
            plugins {
                kotlin("multiplatform")
            }
            
            kotlin {
                jvm()
                js(IR) {
                    browser()
                    nodejs()
                }
                // Add other targets as needed
                sourceSets {
                    val commonMain by getting {
                        dependencies {
                            // Common dependencies
                        }
                    }
                    val commonTest by getting {
                        dependencies {
                            implementation(kotlin("test"))
                        }
                    }
                }
            }
        """.trimIndent()
    }

    private fun createNamespaceDirectories(parent: VirtualFile, namespace: String) {
        var current = parent
        namespace.split('.').forEach { part ->
            current = current.createChildDirectory(this, part)
        }

        // Also create directory with module name
        current.createChildDirectory(this, directoryName)
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
