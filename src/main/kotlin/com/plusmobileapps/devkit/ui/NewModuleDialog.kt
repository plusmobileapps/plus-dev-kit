package com.plusmobileapps.devkit.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*

class NewModuleDialog(
    private val project: Project,
    private val parentDirectory: VirtualFile
) : DialogWrapper(true) {

    companion object {
        private const val PACKAGE_NAME_KEY = "plusdevkit.packageName"
        private const val PUBLIC_BUILD_GRADLE_KEY = "plusdevkit.publicBuildGradle"
        private const val IMPL_BUILD_GRADLE_KEY = "plusdevkit.implBuildGradle"
        private const val TESTING_BUILD_GRADLE_KEY = "plusdevkit.testingBuildGradle"
    }

    private val directoryNameField = JBTextField()
    private val packageNameField = JBTextField()
    private val namespaceField = JBTextField()

    private val publicModuleCheckBox = JBCheckBox("Public", true)
    private val implModuleCheckBox = JBCheckBox("Impl", false)
    private val testingModuleCheckBox = JBCheckBox("Testing", false)

    private val publicBuildGradleArea = JBTextArea(15, 50)
    private val implBuildGradleArea = JBTextArea(15, 50)
    private val testingBuildGradleArea = JBTextArea(15, 50)

    init {
        title = "Create New Module"
        loadPersistedValues()
        setupAutoFillNamespace()
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())

        // Left side - Checkboxes with outlined box
        val checkboxPanel = JPanel()
        checkboxPanel.layout = BoxLayout(checkboxPanel, BoxLayout.Y_AXIS)
        checkboxPanel.add(publicModuleCheckBox)
        checkboxPanel.add(Box.createVerticalStrut(10))
        checkboxPanel.add(implModuleCheckBox)
        checkboxPanel.add(Box.createVerticalStrut(10))
        checkboxPanel.add(testingModuleCheckBox)
        checkboxPanel.add(Box.createVerticalGlue())

        // Add outlined border around checkboxes
        checkboxPanel.border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Create Module Type"
        )

        // Set minimum width to prevent title ellipsization
        val minWidth = 150
        checkboxPanel.minimumSize = Dimension(minWidth, 0)
        checkboxPanel.preferredSize = Dimension(minWidth, checkboxPanel.preferredSize.height)

        // Right side - Text fields
        val fieldsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Directory Name:", directoryNameField)
            .addLabeledComponent("Package Name:", packageNameField)
            .addLabeledComponent("Namespace:", namespaceField)
            .panel

        // Top panel with left and right sections
        val topPanel = JPanel(BorderLayout())
        topPanel.add(checkboxPanel, BorderLayout.WEST)
        topPanel.add(fieldsPanel, BorderLayout.CENTER)

        // Add some spacing
        topPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        // Tabs section for build.gradle.kts templates
        val tabbedPane = JBTabbedPane()

        // Public module tab
        val publicScrollPane = JScrollPane(publicBuildGradleArea)
        tabbedPane.addTab("Public Module", publicScrollPane)

        // Impl module tab
        val implScrollPane = JScrollPane(implBuildGradleArea)
        tabbedPane.addTab("Impl Module", implScrollPane)

        // Testing module tab
        val testingScrollPane = JScrollPane(testingBuildGradleArea)
        tabbedPane.addTab("Testing Module", testingScrollPane)

        // Assemble main panel
        mainPanel.add(topPanel, BorderLayout.NORTH)
        mainPanel.add(tabbedPane, BorderLayout.CENTER)

        mainPanel.preferredSize = JBUI.size(700, 500)

        return mainPanel
    }

    private fun setupAutoFillNamespace() {
        val updateNamespace = {
            val packageName = packageNameField.text.trim()
            val directoryName = directoryNameField.text.trim()

            namespaceField.text = if (packageName.isNotEmpty() && directoryName.isNotEmpty()) {
                "$packageName.$directoryName"
            } else packageName.ifEmpty {
                ""
            }
        }

        packageNameField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                updateNamespace()
            }
        })

        directoryNameField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                updateNamespace()
            }
        })
    }

    private fun loadPersistedValues() {
        val properties = PropertiesComponent.getInstance()

        // Load package name (persisted)
        packageNameField.text = properties.getValue(PACKAGE_NAME_KEY, "com.example")

        // Load build.gradle.kts templates (persisted)
        publicBuildGradleArea.text = properties.getValue(PUBLIC_BUILD_GRADLE_KEY, getDefaultPublicBuildGradle())
        implBuildGradleArea.text = properties.getValue(IMPL_BUILD_GRADLE_KEY, getDefaultImplBuildGradle())
        testingBuildGradleArea.text = properties.getValue(TESTING_BUILD_GRADLE_KEY, getDefaultTestingBuildGradle())

        // Directory name is not persisted - starts empty each time
        directoryNameField.text = ""

        // Update namespace based on loaded values
        val packageName = packageNameField.text.trim()
        if (packageName.isNotEmpty()) {
            namespaceField.text = packageName
        }
    }

    private fun savePersistedValues() {
        val properties = PropertiesComponent.getInstance()

        // Save package name and build.gradle.kts templates
        properties.setValue(PACKAGE_NAME_KEY, packageNameField.text)
        properties.setValue(PUBLIC_BUILD_GRADLE_KEY, publicBuildGradleArea.text)
        properties.setValue(IMPL_BUILD_GRADLE_KEY, implBuildGradleArea.text)
        properties.setValue(TESTING_BUILD_GRADLE_KEY, testingBuildGradleArea.text)
    }

    override fun doOKAction() {
        savePersistedValues()
        super.doOKAction()
    }

    override fun doValidate(): ValidationInfo? {
        if (directoryNameField.text.isNullOrBlank()) {
            return ValidationInfo("Directory name is required", directoryNameField)
        }

        if (packageNameField.text.isNullOrBlank()) {
            return ValidationInfo("Package name is required", packageNameField)
        }

        if (!publicModuleCheckBox.isSelected && !implModuleCheckBox.isSelected && !testingModuleCheckBox.isSelected) {
            return ValidationInfo("At least one module type must be selected", publicModuleCheckBox)
        }

        return null
    }

    fun createModule() {
        val packageName = packageNameField.text.trim()
        val directoryName = directoryNameField.text.trim()

        if (packageName.isEmpty() || directoryName.isEmpty()) {
            return
        }

        // Use packageName.directoryName as namespace to create correct directory structure
        val namespace = "$packageName.$directoryName"

        // Calculate project directory path relative to project root
        val projectBasePath = project.basePath
        val parentPath = parentDirectory.path
        val projectDirectory = if (projectBasePath != null && parentPath.startsWith(projectBasePath)) {
            // Get relative path from project root, ensuring it starts with ":"
            val relativePath = parentPath.substring(projectBasePath.length)
                .replace("/", ":")
                .let { if (it.startsWith(":")) it else ":$it" }
            relativePath
        } else {
            // Fallback if we can't determine relative path
            ":${parentDirectory.name}"
        }

        // Replace placeholders in templates with actual values
        val processedPublicTemplate = if (publicModuleCheckBox.isSelected) {
            publicBuildGradleArea.text
                .replace("\$directoryName", directoryName)
                .replace("\$projectDirectory", projectDirectory)
        } else null

        val processedImplTemplate = if (implModuleCheckBox.isSelected) {
            implBuildGradleArea.text
                .replace("\$directoryName", directoryName)
                .replace("\$projectDirectory", projectDirectory)
        } else null

        val processedTestingTemplate = if (testingModuleCheckBox.isSelected) {
            testingBuildGradleArea.text
                .replace("\$directoryName", directoryName)
                .replace("\$projectDirectory", projectDirectory)
        } else null

        val moduleCreator = ModuleCreator(
            project = project,
            parentDirectory = parentDirectory,
            namespace = namespace,
            directoryName = directoryName,
            createPublic = publicModuleCheckBox.isSelected,
            createImpl = implModuleCheckBox.isSelected,
            createTesting = testingModuleCheckBox.isSelected,
            publicBuildGradleTemplate = processedPublicTemplate,
            implBuildGradleTemplate = processedImplTemplate,
            testingBuildGradleTemplate = processedTestingTemplate
        )

        moduleCreator.createModules()
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
                            implementation(project("${'$'}projectDirectory:${'$'}directoryName:public"))
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
                            implementation(project("${'$'}projectDirectory:${'$'}directoryName:public"))
                            implementation(project("${'$'}projectDirectory:${'$'}directoryName:impl"))
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
}
