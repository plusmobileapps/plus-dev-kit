package com.plusmobileapps.devkit.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.icons.AllIcons

class NewFeatureModuleAction : AnAction("New Feature Module", "Create a new module", AllIcons.Actions.ModuleDirectory) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val viewModel = NewFeatureModuleViewModel(project, selectedFile)
        val dialog = NewModuleDialog(viewModel)
        if (dialog.showAndGet()) {
            viewModel.createModule()
        }
    }

    override fun update(e: AnActionEvent) {
        // Enable the action only for directories
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file != null && file.isDirectory
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
