package com.plusmobileapps.devkit.actions

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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
    private val viewModel: NewFeatureModuleViewModel
) : DialogWrapper(true) {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())


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
        scope.launch {
            viewModel.state.collect {
                updateUi(it)
            }
        }
        observeUiEvents()
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())

        // Left side - Checkboxes with outlined box
        val checkboxPanel = createCheckBoxPanel()

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

    private fun createCheckBoxPanel(): JPanel = JPanel().apply {
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
        preferredSize = Dimension(minWidth, preferredSize.height)
    }

    private fun observeUiEvents() {
        namespaceField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                viewModel.onNameSpaceUpdated(namespaceField.text)
            }
        })

        directoryNameField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                viewModel.onDirectoryNameUpdated(directoryNameField.text)
            }
        })

        packageNameField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                viewModel.onPackageNameUpdated(packageNameField.text)
            }
        })

        publicModuleCheckBox.addActionListener {
            viewModel.onCreatePublicUpdated(publicModuleCheckBox.isSelected)
        }

        implModuleCheckBox.addActionListener {
            viewModel.onCreateImplUpdated(implModuleCheckBox.isSelected)
        }

        testingModuleCheckBox.addActionListener {
            viewModel.onCreateTestingUpdated(testingModuleCheckBox.isSelected)
        }

        publicBuildGradleArea.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                viewModel.onPublicBuildGradleUpdated(publicBuildGradleArea.text)
            }
        })

        implBuildGradleArea.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                viewModel.onImplBuildGradleUpdated(implBuildGradleArea.text)
            }
        })

        testingBuildGradleArea.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent?) {
                viewModel.onTestingBuildGradleUpdated(testingBuildGradleArea.text)
            }
        })
    }

    override fun doOKAction() {
        viewModel.savePersistedValues()
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

    private fun updateUi(state: NewFeatureModuleViewModel.State) {
        // Update text areas for build.gradle.kts templates
        publicBuildGradleArea.text = state.publicBuildGradle
        implBuildGradleArea.text = state.implBuildGradle
        testingBuildGradleArea.text = state.testingBuildGradle

        // Update text fields
        directoryNameField.text = state.directoryName
        namespaceField.text = state.namespace
        packageNameField.text = state.packageName


        // Checkboxes for which modules to create
        publicModuleCheckBox.isSelected = state.createPublic
        implModuleCheckBox.isSelected = state.createImpl
        testingModuleCheckBox.isSelected = state.createTesting
    }

    override fun dispose() {
        scope.cancel()
        super.dispose()
    }
}
