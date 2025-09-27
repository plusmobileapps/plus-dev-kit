package com.plusmobileapps.devkit.newfeature

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
import com.plusmobileapps.devkit.newfeature.BuildGradleDefaults.getDefaultImplBuildGradle
import com.plusmobileapps.devkit.newfeature.BuildGradleDefaults.getDefaultPublicBuildGradle
import com.plusmobileapps.devkit.newfeature.BuildGradleDefaults.getDefaultTestingBuildGradle
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

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
        val topPanel: JPanel = createTopPanel()
        val tabbedPane: JBTabbedPane = createTabbedPane()

        // Assemble main panel
        mainPanel.add(topPanel, BorderLayout.NORTH)
        mainPanel.add(tabbedPane, BorderLayout.CENTER)
        mainPanel.preferredSize = JBUI.size(700, 500)
        return mainPanel
    }

    private fun createTopPanel(): JPanel = JPanel(BorderLayout()).apply {
        // Left side - Checkboxes with outlined box
        val checkboxPanel: JPanel = createCheckboxPannel()

        // Right side - Text fields
        val fieldsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Directory Name:", directoryNameField)
            .addLabeledComponent("Package Name:", packageNameField)
            .addLabeledComponent("Namespace:", namespaceField)
            .panel

        // Top panel with left and right sections
        add(checkboxPanel, BorderLayout.WEST)
        add(fieldsPanel, BorderLayout.CENTER)
        // Add some spacing
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    }

    private fun createTabbedPane(): JBTabbedPane = JBTabbedPane().apply {
        // Public module tab
        val publicScrollPane = JScrollPane(publicBuildGradleArea)
        addTab("Public Module", publicScrollPane)

        // Impl module tab
        val implScrollPane = JScrollPane(implBuildGradleArea)
        addTab("Impl Module", implScrollPane)

        // Testing module tab
        val testingScrollPane = JScrollPane(testingBuildGradleArea)
        addTab("Testing Module", testingScrollPane)
    }

    private fun createCheckboxPannel(): JPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(publicModuleCheckBox)
        add(Box.createVerticalStrut(10))
        add(implModuleCheckBox)
        add(Box.createVerticalStrut(10))
        add(testingModuleCheckBox)
        add(Box.createVerticalGlue())

        // Add outlined border around checkboxes
        border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Create Module Type"
        )

        // Set minimum width to prevent title ellipsization
        val minWidth = 150
        minimumSize = Dimension(minWidth, 0)
        preferredSize = Dimension(minWidth, this.preferredSize.height)
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
        val namespace = namespaceField.text

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
                .replace("\$namespace", namespace)
        } else null

        val processedImplTemplate = if (implModuleCheckBox.isSelected) {
            implBuildGradleArea.text
                .replace("\$directoryName", directoryName)
                .replace("\$projectDirectory", projectDirectory)
                .replace("\$namespace", namespace)
        } else null

        val processedTestingTemplate = if (testingModuleCheckBox.isSelected) {
            testingBuildGradleArea.text
                .replace("\$directoryName", directoryName)
                .replace("\$projectDirectory", projectDirectory)
                .replace("\$namespace", namespace)
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
}
