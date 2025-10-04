package com.plusmobileapps.devkit.newfeature

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
    private val createTesting: Boolean,
    private val publicBuildGradleTemplate: String? = null,
    private val implBuildGradleTemplate: String? = null,
    private val testingBuildGradleTemplate: String? = null
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

                val createdModules = mutableListOf<String>()

                // Create modules based on selected options
                if (createPublic) {
                    createModuleDirectory(mainDir, PUBLIC_MODULE_NAME)
                    createdModules.add(":$directoryName:$PUBLIC_MODULE_NAME")
                }
                if (createImpl) {
                    createModuleDirectory(mainDir, IMPL_MODULE_NAME)
                    createdModules.add(":$directoryName:$IMPL_MODULE_NAME")
                }
                if (createTesting) {
                    createModuleDirectory(mainDir, TESTING_MODULE_NAME)
                    createdModules.add(":$directoryName:$TESTING_MODULE_NAME")
                }

                // Add modules to settings.gradle.kts
                if (createdModules.isNotEmpty()) {
                    addModulesToSettings(createdModules)
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

        // Create build.gradle.kts file with custom template
        val buildGradleContent = when (moduleName) {
            PUBLIC_MODULE_NAME -> publicBuildGradleTemplate ?: getDefaultPublicBuildGradle()
            IMPL_MODULE_NAME -> implBuildGradleTemplate ?: getDefaultImplBuildGradle()
            TESTING_MODULE_NAME -> testingBuildGradleTemplate ?: getDefaultTestingBuildGradle()
            else -> getDefaultPublicBuildGradle()
        }

        val buildGradleFile = moduleDir.createChildData(this, "build.gradle.kts")
        buildGradleFile.setBinaryContent(buildGradleContent.toByteArray())
    }

    private fun getDefaultPublicBuildGradle(): String {
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
                
                sourceSets {
                    val commonMain by getting {
                        dependencies {
                            // Public module dependencies
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

    private fun getDefaultImplBuildGradle(): String {
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
                
                sourceSets {
                    val commonMain by getting {
                        dependencies {
                            implementation(project(":shared:$directoryName:public"))
                            // Implementation module dependencies
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

    private fun getDefaultTestingBuildGradle(): String {
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
                
                sourceSets {
                    val commonMain by getting {
                        dependencies {
                            implementation(project(":shared:$directoryName:public"))
                            implementation(project(":shared:$directoryName:impl"))
                            // Testing module dependencies
                            implementation(kotlin("test"))
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
        // Removed the extra directoryName directory creation
    }

    private fun showNotification(content: String, type: NotificationType) {
        ApplicationManager.getApplication().invokeLater {
            NotificationGroupManager.getInstance()
                .getNotificationGroup("Plus Dev Kit Notifications")
                .createNotification(content, type)
                .notify(project)
        }
    }

    private fun addModulesToSettings(moduleNames: List<String>) {
        try {
            // Find settings.gradle.kts in the project root
            val projectBaseDir = project.baseDir ?: return
            val settingsFile = projectBaseDir.findChild("settings.gradle.kts") ?: return

            // Calculate the relative path from project root to the parent directory
            val relativePath = projectBaseDir.toNioPath().relativize(parentDirectory.toNioPath())
            val pathPrefix = if (relativePath.toString().isEmpty()) "" else ":${relativePath.toString().replace("/", ":")}"

            val currentContent = String(settingsFile.contentsToByteArray())

            // Create new module includes with correct path
            val newModules = moduleNames.map { moduleName ->
                "$pathPrefix$moduleName"
            }

            // Parse existing includes and add new ones
            val allIncludes = mutableSetOf<String>()

            // Extract existing includes from current content
            currentContent.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("include(") && trimmed.contains("\"")) {
                    val match = Regex("include\\(\"([^\"]+)\"\\)").find(trimmed)
                    match?.groupValues?.get(1)?.let { allIncludes.add(it) }
                }
            }

            // Add new modules
            allIncludes.addAll(newModules)

            // Remove include lines from current content
            val contentWithoutIncludes = currentContent.lines()
                .filterNot { line ->
                    val trimmed = line.trim()
                    trimmed.startsWith("include(") && trimmed.contains("\"")
                }
                .joinToString("\n")

            // Sort includes and create final content
            val sortedIncludes = allIncludes.sorted().joinToString("\n") { "include(\"$it\")" }

            val updatedContent = if (contentWithoutIncludes.trim().isEmpty()) {
                sortedIncludes
            } else {
                // Only add one newline if the content doesn't already end with one
                val separator = if (contentWithoutIncludes.endsWith("\n")) "\n" else "\n\n"
                "$contentWithoutIncludes$separator$sortedIncludes"
            }

            settingsFile.setBinaryContent(updatedContent.toByteArray())

            showNotification("Added modules to settings.gradle.kts", NotificationType.INFORMATION)
        } catch (e: Exception) {
            showNotification("Warning: Could not update settings.gradle.kts: ${e.message}", NotificationType.WARNING)
        }
    }
}
