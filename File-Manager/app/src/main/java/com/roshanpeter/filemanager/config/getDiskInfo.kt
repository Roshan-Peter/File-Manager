package com.roshanpeter.filemanager.config

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.roshanpeter.filemanager.dataClass.DiskInfo
import java.io.File
import kotlin.math.roundToInt

fun getDisks(context: Context): List<DiskInfo> {
    val disks = mutableListOf<DiskInfo>()

    val internal = Environment.getExternalStorageDirectory().canonicalFile
    disks.add(createDiskInfo("Internal Storage", internal))

    val externalDirs = context.getExternalFilesDirs(null)

    for (dir in externalDirs) {
        if (dir == null) continue

        var root = dir
        repeat(4) { root = root.parentFile ?: root }
        root = root.canonicalFile

        if (root != internal && root.exists() && root.canRead()) {
            val name = if (root.name.lowercase().contains("usb")) "USB Drive" else "External Storage"
            disks.add(createDiskInfo(name, root))
        }
    }

    return disks
}


fun createDiskInfo(name: String, file: File): DiskInfo {
    val stat = StatFs(file.absolutePath)
    val totalGB = (stat.totalBytes / 1_073_741_824.0).roundToInt()
    val freeGB = (stat.availableBytes / 1_073_741_824.0).roundToInt()
    return DiskInfo(name, file, totalGB, freeGB)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(context: Context, onDiskClick: (File) -> Unit) {
    val disks = remember { getDisks(context) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Select Storage") }) }
    ) { padding ->
        Column {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                items(disks) { disk ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .clickable { onDiskClick(disk.path) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(disk.name, style = MaterialTheme.typography.titleMedium)
                            Text("Total: ${disk.totalGB} GB | Free: ${disk.freeGB} GB")
                            Text(disk.path.absolutePath, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}