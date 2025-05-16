package com.roshanpeter.filemanager

import java.io.File

object FileClipboard {
    var files: List<File> = emptyList()
    var isCut: Boolean = false
    val hasItems: Boolean get() = files.isNotEmpty()

    fun clear() {
        files = emptyList()
        isCut = false
    }
}