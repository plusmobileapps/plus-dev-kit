package com.plusmobileapps.devkit.newfeature

import com.intellij.openapi.vfs.VfsUtil
import java.io.IOException
import com.intellij.openapi.vfs.LocalFileSystem;
import java.io.File;

// Assuming 'virtualFile' is a non-null VirtualFile instance

fun readFileContent(path: String): String? {
    val ioFile = File(path)
    val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(ioFile) ?: return null
    // VfsUtil.loadText returns a CharSequence
    return try {
        val content: CharSequence = VfsUtil.loadText(virtualFile)
        content.toString()
    } catch (e: IOException) {
        // Log or handle the exception if the file can't be read
        e.printStackTrace()
        null
    }
}