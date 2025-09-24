package com.plusmobileapps.devkit.actions

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NewFeatureModuleViewModel(
    private val project: Project,
    private val parentDirectory: VirtualFile,
) {

    val _state = MutableStateFlow(
        loadPersistedValues()
    )

    val state: StateFlow<State> = _state.asStateFlow()

    private fun loadPersistedValues(): State {
        val properties: PropertiesComponent = PropertiesComponent.getInstance()
        return State(
            packageName = properties.getValue(PACKAGE_NAME_KEY, "com.example"),
            directoryName = "",
            namespace = properties.getValue(PACKAGE_NAME_KEY, "com.example"),
            createPublic = true,
            createImpl = true,
            createTesting = false,
            publicBuildGradle = properties.getValue(PUBLIC_BUILD_GRADLE_KEY, NewFeatureModuleDefaults.getDefaultPublicBuildGradle()),
            implBuildGradle = properties.getValue(IMPL_BUILD_GRADLE_KEY, NewFeatureModuleDefaults.getDefaultImplBuildGradle()),
            testingBuildGradle = properties.getValue(TESTING_BUILD_GRADLE_KEY, NewFeatureModuleDefaults.getDefaultTestingBuildGradle())
        )
    }

    fun onNameSpaceUpdated(nameSpace: String) {
        _state.update {
            it.copy(namespace = nameSpace)
        }
    }

    fun onPackageNameUpdated(packageName: String) {
        _state.update {
            it.copy(packageName = packageName)
        }
        updateNamespace()
    }

    fun onDirectoryNameUpdated(directoryName: String) {
        _state.update {
            it.copy(directoryName = directoryName)
        }
        updateNamespace()
    }

    fun onCreatePublicUpdated(createPublic: Boolean) {
        _state.update {
            it.copy(createPublic = createPublic)
        }
    }

    fun onCreateImplUpdated(createImpl: Boolean) {
        _state.update {
            it.copy(createImpl = createImpl)
        }
    }

    fun onCreateTestingUpdated(createTesting: Boolean) {
        _state.update {
            it.copy(createTesting = createTesting)
        }
    }

    fun onPublicBuildGradleUpdated(publicBuildGradle: String) {
        _state.update {
            it.copy(publicBuildGradle = publicBuildGradle)
        }
    }

    fun onImplBuildGradleUpdated(implBuildGradle: String) {
        _state.update {
            it.copy(implBuildGradle = implBuildGradle)
        }
    }

    fun onTestingBuildGradleUpdated(testingBuildGradle: String) {
        _state.update {
            it.copy(testingBuildGradle = testingBuildGradle)
        }
    }

    fun savePersistedValues() {
        val properties = PropertiesComponent.getInstance()
        val currentState = state.value

        // Save package name and build.gradle.kts templates
        properties.setValue(PACKAGE_NAME_KEY, currentState.packageName)
        properties.setValue(PUBLIC_BUILD_GRADLE_KEY, currentState.publicBuildGradle)
        properties.setValue(IMPL_BUILD_GRADLE_KEY, currentState.implBuildGradle)
        properties.setValue(TESTING_BUILD_GRADLE_KEY, currentState.testingBuildGradle)
    }

    fun createModule() {
//        val packageName = packageNameField.text.trim()
//        val directoryName = directoryNameField.text.trim()
//
//        if (packageName.isEmpty() || directoryName.isEmpty()) {
//            return
//        }
//
//        // Use packageName.directoryName as namespace to create correct directory structure
//        val namespace = "$packageName.$directoryName"
//
//        // Calculate project directory path relative to project root
//        val projectBasePath = project.basePath
//        val parentPath = parentDirectory.path
//        val projectDirectory = if (projectBasePath != null && parentPath.startsWith(projectBasePath)) {
//            // Get relative path from project root, ensuring it starts with ":"
//            val relativePath = parentPath.substring(projectBasePath.length)
//                .replace("/", ":")
//                .let { if (it.startsWith(":")) it else ":$it" }
//            relativePath
//        } else {
//            // Fallback if we can't determine relative path
//            ":${parentDirectory.name}"
//        }
//
//        // Replace placeholders in templates with actual values
//        val processedPublicTemplate = if (publicModuleCheckBox.isSelected) {
//            publicBuildGradleArea.text
//                .replace("\$directoryName", directoryName)
//                .replace("\$projectDirectory", projectDirectory)
//        } else null
//
//        val processedImplTemplate = if (implModuleCheckBox.isSelected) {
//            implBuildGradleArea.text
//                .replace("\$directoryName", directoryName)
//                .replace("\$projectDirectory", projectDirectory)
//        } else null
//
//        val processedTestingTemplate = if (testingModuleCheckBox.isSelected) {
//            testingBuildGradleArea.text
//                .replace("\$directoryName", directoryName)
//                .replace("\$projectDirectory", projectDirectory)
//        } else null
//
//        val moduleCreator = ModuleCreator(
//            project = project,
//            parentDirectory = parentDirectory,
//            namespace = namespace,
//            directoryName = directoryName,
//            createPublic = publicModuleCheckBox.isSelected,
//            createImpl = implModuleCheckBox.isSelected,
//            createTesting = testingModuleCheckBox.isSelected,
//            publicBuildGradleTemplate = processedPublicTemplate,
//            implBuildGradleTemplate = processedImplTemplate,
//            testingBuildGradleTemplate = processedTestingTemplate
//        )
//
//        moduleCreator.createModules()
    }

   private fun updateNamespace() {
       val currentState = state.value
        val packageName = currentState.packageName.trim()
        val directoryName = currentState.directoryName.trim()

       _state.update {
           it.copy(
               namespace = if (packageName.isNotEmpty() && directoryName.isNotEmpty()) {
                   "$packageName.$directoryName"
               } else packageName.ifEmpty {
                   ""
               }
           )
       }
    }

    data class State(
        val packageName: String,
        val directoryName: String,
        val namespace: String,
        val createPublic: Boolean,
        val createImpl: Boolean,
        val createTesting: Boolean,
        val publicBuildGradle: String,
        val implBuildGradle: String,
        val testingBuildGradle: String
    )

    companion object {
        private const val PACKAGE_NAME_KEY = "plusdevkit.packageName"
        private const val PUBLIC_BUILD_GRADLE_KEY = "plusdevkit.publicBuildGradle"
        private const val IMPL_BUILD_GRADLE_KEY = "plusdevkit.implBuildGradle"
        private const val TESTING_BUILD_GRADLE_KEY = "plusdevkit.testingBuildGradle"
    }
}