package com.plusmobileapps.devkit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import org.jetbrains.jewel.bridge.theme.SwingBridgeTheme
import org.jetbrains.jewel.ui.component.Text
import javax.swing.JComponent

class NewModuleComposeDialog(
    private val project: Project,
    private val parentDirectory: VirtualFile
) : DialogWrapper(true) {

    companion object {
        private const val PACKAGE_NAME_KEY = "plusdevkit.packageName"
        private const val PUBLIC_BUILD_GRADLE_KEY = "plusdevkit.publicBuildGradle"
        private const val IMPL_BUILD_GRADLE_KEY = "plusdevkit.implBuildGradle"
        private const val TESTING_BUILD_GRADLE_KEY = "plusdevkit.testingBuildGradle"
    }

    private var directoryName by mutableStateOf("")
    private var packageName by mutableStateOf("")
    private var namespace by mutableStateOf("")

    private var publicModuleSelected by mutableStateOf(true)
    private var implModuleSelected by mutableStateOf(false)
    private var testingModuleSelected by mutableStateOf(false)

    private var publicBuildGradle by mutableStateOf("")
    private var implBuildGradle by mutableStateOf("")
    private var testingBuildGradle by mutableStateOf("")

    init {
        title = "Create New Module (Compose)"
        loadPersistedValues()
        init()
    }

    override fun createCenterPanel(): JComponent {
        return ComposePanel().apply {
            setContent {
                SwingBridgeTheme {
                    NewModuleContent(
                        directoryName = directoryName,
                        onDirectoryNameChange = {
                            directoryName = it
                            updateNamespace()
                        },
                        packageName = packageName,
                        onPackageNameChange = {
                            packageName = it
                            updateNamespace()
                        },
                        namespace = namespace,
                        publicModuleSelected = publicModuleSelected,
                        onPublicModuleChange = { publicModuleSelected = it },
                        implModuleSelected = implModuleSelected,
                        onImplModuleChange = { implModuleSelected = it },
                        testingModuleSelected = testingModuleSelected,
                        onTestingModuleChange = { testingModuleSelected = it },
                        publicBuildGradle = publicBuildGradle,
                        onPublicBuildGradleChange = { publicBuildGradle = it },
                        implBuildGradle = implBuildGradle,
                        onImplBuildGradleChange = { implBuildGradle = it },
                        testingBuildGradle = testingBuildGradle,
                        onTestingBuildGradleChange = { testingBuildGradle = it }
                    )
                }
            }
        }
    }

    private fun updateNamespace() {
        namespace = if (packageName.isNotEmpty() && directoryName.isNotEmpty()) {
            "$packageName.$directoryName"
        } else packageName.ifEmpty { "" }
    }

    private fun loadPersistedValues() {
        val properties = PropertiesComponent.getInstance()

        packageName = properties.getValue(PACKAGE_NAME_KEY, "com.example")
        publicBuildGradle = properties.getValue(PUBLIC_BUILD_GRADLE_KEY, getDefaultPublicBuildGradle())
        implBuildGradle = properties.getValue(IMPL_BUILD_GRADLE_KEY, getDefaultImplBuildGradle())
        testingBuildGradle = properties.getValue(TESTING_BUILD_GRADLE_KEY, getDefaultTestingBuildGradle())

        directoryName = ""

        if (packageName.isNotEmpty()) {
            namespace = packageName
        }
    }

    private fun savePersistedValues() {
        val properties = PropertiesComponent.getInstance()

        properties.setValue(PACKAGE_NAME_KEY, packageName)
        properties.setValue(PUBLIC_BUILD_GRADLE_KEY, publicBuildGradle)
        properties.setValue(IMPL_BUILD_GRADLE_KEY, implBuildGradle)
        properties.setValue(TESTING_BUILD_GRADLE_KEY, testingBuildGradle)
    }

    override fun doOKAction() {
        savePersistedValues()
        super.doOKAction()
    }

    override fun doValidate(): ValidationInfo? {
        if (directoryName.isBlank()) {
            return ValidationInfo("Directory name is required")
        }

        if (packageName.isBlank()) {
            return ValidationInfo("Package name is required")
        }

        if (!publicModuleSelected && !implModuleSelected && !testingModuleSelected) {
            return ValidationInfo("At least one module type must be selected")
        }

        return null
    }

    fun createModule() {
        if (packageName.isEmpty() || directoryName.isEmpty()) {
            return
        }

        val namespace = "$packageName.$directoryName"

        val projectBasePath = project.basePath
        val parentPath = parentDirectory.path
        val projectDirectory = if (projectBasePath != null && parentPath.startsWith(projectBasePath)) {
            val relativePath = parentPath.substring(projectBasePath.length)
                .replace("/", ":")
                .let { if (it.startsWith(":")) it else ":$it" }
            relativePath
        } else {
            ":${parentDirectory.name}"
        }

        val processedPublicTemplate = if (publicModuleSelected) {
            publicBuildGradle
                .replace("\$directoryName", directoryName)
                .replace("\$projectDirectory", projectDirectory)
        } else null

        val processedImplTemplate = if (implModuleSelected) {
            implBuildGradle
                .replace("\$directoryName", directoryName)
                .replace("\$projectDirectory", projectDirectory)
        } else null

        val processedTestingTemplate = if (testingModuleSelected) {
            testingBuildGradle
                .replace("\$directoryName", directoryName)
                .replace("\$projectDirectory", projectDirectory)
        } else null

        val moduleCreator = ModuleCreator(
            project = project,
            parentDirectory = parentDirectory,
            namespace = namespace,
            directoryName = directoryName,
            createPublic = publicModuleSelected,
            createImpl = implModuleSelected,
            createTesting = testingModuleSelected,
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

@Composable
fun NewModuleContent(
    directoryName: String,
    onDirectoryNameChange: (String) -> Unit,
    packageName: String,
    onPackageNameChange: (String) -> Unit,
    namespace: String,
    publicModuleSelected: Boolean,
    onPublicModuleChange: (Boolean) -> Unit,
    implModuleSelected: Boolean,
    onImplModuleChange: (Boolean) -> Unit,
    testingModuleSelected: Boolean,
    onTestingModuleChange: (Boolean) -> Unit,
    publicBuildGradle: String,
    onPublicBuildGradleChange: (String) -> Unit,
    implBuildGradle: String,
    onImplBuildGradleChange: (String) -> Unit,
    testingBuildGradle: String,
    onTestingBuildGradleChange: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    val backgroundColor = Color(JBColor.PanelBackground.rgb)
    val textColor = Color(JBColor.foreground().rgb)
    val borderColor = Color(JBColor.border().rgb)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top section with form fields and checkboxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left side - Checkboxes
            GroupBox(
                title = "Create Module Type",
                modifier = Modifier.width(200.dp),
                textColor = textColor,
                borderColor = borderColor
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CheckboxRow(
                        text = "Public",
                        checked = publicModuleSelected,
                        onCheckedChange = onPublicModuleChange,
                        textColor = textColor
                    )

                    CheckboxRow(
                        text = "Impl",
                        checked = implModuleSelected,
                        onCheckedChange = onImplModuleChange,
                        textColor = textColor
                    )

                    CheckboxRow(
                        text = "Testing",
                        checked = testingModuleSelected,
                        onCheckedChange = onTestingModuleChange,
                        textColor = textColor
                    )
                }
            }

            // Right side - Text fields
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextFieldWithLabel(
                    label = "Directory Name",
                    value = directoryName,
                    onValueChange = onDirectoryNameChange,
                    textColor = textColor,
                    borderColor = borderColor
                )

                TextFieldWithLabel(
                    label = "Package Name",
                    value = packageName,
                    onValueChange = onPackageNameChange,
                    textColor = textColor,
                    borderColor = borderColor
                )

                TextFieldWithLabel(
                    label = "Namespace",
                    value = namespace,
                    onValueChange = { /* Read-only */ },
                    readOnly = true,
                    textColor = textColor,
                    borderColor = borderColor
                )
            }
        }

        // Tabs section for build.gradle.kts templates
        TabStrip(
            selectedTabIndex = selectedTab,
            onTabSelected = { selectedTab = it },
            tabs = listOf("Public Module", "Impl Module", "Testing Module"),
            textColor = textColor,
            borderColor = borderColor
        )

        // Tab content
        when (selectedTab) {
            0 -> BuildGradleEditor(
                value = publicBuildGradle,
                onValueChange = onPublicBuildGradleChange,
                modifier = Modifier.fillMaxSize(),
                textColor = textColor,
                borderColor = borderColor
            )

            1 -> BuildGradleEditor(
                value = implBuildGradle,
                onValueChange = onImplBuildGradleChange,
                modifier = Modifier.fillMaxSize(),
                textColor = textColor,
                borderColor = borderColor
            )

            2 -> BuildGradleEditor(
                value = testingBuildGradle,
                onValueChange = onTestingBuildGradleChange,
                modifier = Modifier.fillMaxSize(),
                textColor = textColor,
                borderColor = borderColor
            )
        }
    }
}

@Composable
fun CheckboxRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .border(1.dp, textColor, RoundedCornerShape(2.dp))
                .background(
                    if (checked) textColor else Color.Transparent,
                    RoundedCornerShape(2.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, RoundedCornerShape(1.dp))
                )
            }
        }
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp
        )
    }
}

@Composable
fun TextFieldWithLabel(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    textColor: Color,
    borderColor: Color,
    readOnly: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp
        )
        BasicTextField(
            value = value,
            onValueChange = if (readOnly) {
                {}
            } else onValueChange,
            textStyle = TextStyle(
                color = textColor,
                fontSize = 14.sp
            ),
            cursorBrush = SolidColor(textColor),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                .padding(8.dp)
        )
    }
}

@Composable
fun TabStrip(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<String>,
    textColor: Color,
    borderColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
    ) {
        tabs.forEachIndexed { index, title ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(index) }
                    .background(
                        if (selectedTabIndex == index) borderColor.copy(alpha = 0.2f) else Color.Transparent
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun BuildGradleEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color,
    borderColor: Color
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .padding(top = 8.dp)
            .fillMaxSize()
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        textStyle = TextStyle(
            color = textColor,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        ),
        cursorBrush = SolidColor(textColor)
    )
}

@Composable
fun GroupBox(
    title: String,
    modifier: Modifier = Modifier,
    textColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            color = textColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            content()
        }
    }
}
