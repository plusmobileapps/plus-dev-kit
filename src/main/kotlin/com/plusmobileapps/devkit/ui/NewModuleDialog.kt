package com.plusmobileapps.devkit.ui

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.border.TitledBorder

class NewModuleDialog(
    private val project: Project,
    private val parentDirectory: VirtualFile
) : DialogWrapper(true) {

    companion object {
        private const val PACKAGE_NAME_KEY = "plusdevkit.packageName"
        private const val DIRECTORY_NAME_KEY = "plusdevkit.directoryName"
    }

    private val packageNameField = JBTextField()
    private val directoryNameField = JBTextField()

    private val publicModuleCheckBox = JBCheckBox("Public", true)
    private val implModuleCheckBox = JBCheckBox("Impl", false)
    private val testingModuleCheckBox = JBCheckBox("Testing", false)

    init {
        title = "Create New Module"
        loadPersistedValues()
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()

        // Create checkboxes panel (left side)
        val checkboxesPanel = JPanel(GridLayout(3, 1, 0, 10))
        checkboxesPanel.add(publicModuleCheckBox)
        checkboxesPanel.add(implModuleCheckBox)
        checkboxesPanel.add(testingModuleCheckBox)

        // Add border with title
        checkboxesPanel.border = TitledBorder("Module types")

        // Position checkboxes on the left
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.weightx = 0.3
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.NORTHWEST
        gbc.insets = JBUI.insets(10)
        mainPanel.add(checkboxesPanel, gbc)

        // Create text fields panel (right side)
        val textFieldsPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Package Name:", packageNameField)
            .addLabeledComponent("Directory name:", directoryNameField)
            .panel

        // Add border with title
        textFieldsPanel.border = TitledBorder("Module details")

        // Position text fields on the right
        gbc.gridx = 1
        gbc.gridy = 0
        gbc.weightx = 0.7
        gbc.weighty = 1.0
        gbc.fill = GridBagConstraints.BOTH
        mainPanel.add(textFieldsPanel, gbc)

        // Set preferred size
        mainPanel.preferredSize = JBUI.size(500, 200)

        return mainPanel
    }

    private fun loadPersistedValues() {
        val properties = PropertiesComponent.getInstance()
        packageNameField.text = properties.getValue(PACKAGE_NAME_KEY, "com.example")
        directoryNameField.text = properties.getValue(DIRECTORY_NAME_KEY, "")
    }

    private fun savePersistedValues() {
        val properties = PropertiesComponent.getInstance()
        properties.setValue(PACKAGE_NAME_KEY, packageNameField.text)
        properties.setValue(DIRECTORY_NAME_KEY, directoryNameField.text)
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

        // Use just the packageName as namespace, not packageName.directoryName
        val namespace = packageName

        val moduleCreator = ModuleCreator(
            project = project,
            parentDirectory = parentDirectory,
            namespace = namespace,
            directoryName = directoryName,
            createPublic = publicModuleCheckBox.isSelected,
            createImpl = implModuleCheckBox.isSelected,
            createTesting = testingModuleCheckBox.isSelected
        )

        moduleCreator.createModules()
    }
}
