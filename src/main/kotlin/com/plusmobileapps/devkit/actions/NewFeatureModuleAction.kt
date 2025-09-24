package com.plusmobileapps.devkit.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.plusmobileapps.devkit.ui.NewModuleComposeDialog

class NewFeatureModuleAction : AnAction("New Feature Module", "Create a new module", AllIcons.Actions.ModuleDirectory) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val dialog = NewModuleComposeDialog(project, selectedFile)
        if (dialog.showAndGet()) {
            // Dialog was confirmed, process the input
            dialog.createModule()
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
