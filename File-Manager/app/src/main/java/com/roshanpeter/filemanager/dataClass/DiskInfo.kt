package com.roshanpeter.filemanager.dataClass

import java.io.File

data class DiskInfo(
    val name: String,
    val path: File,
    val totalGB: Int,
    val freeGB: Int
)