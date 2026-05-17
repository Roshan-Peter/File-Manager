package com.roshanpeter.filemanager.pages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPasteGo
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.roshanpeter.filemanager.FileClipboard
import com.roshanpeter.filemanager.ui.theme.FileManagerTheme
import java.io.File


class FileExplorerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val folderPath = intent.getStringExtra("folder_path")
        val startDir = File(folderPath ?: Environment.getExternalStorageDirectory().absolutePath)

        setContent {
            FileManagerTheme {
                var refreshTrigger by remember { mutableStateOf(0) }

                FileManagerTheme {
                    FileExplorerScreen(
                        currentDir = startDir,
                        onItemClick = { file ->
                            if (file.isDirectory) {
                                val intent = Intent(this, FileExplorerActivity::class.java)
                                intent.putExtra("folder_path", file.absolutePath)
                                startActivity(intent)
                            } else {
                                openFile(this, file)
                            }
                        },
                        onRefresh = {
                            refreshTrigger++
                        },
                        refreshTrigger = refreshTrigger
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileExplorerScreen(
    currentDir: File,
    onItemClick: (File) -> Unit,
    onRefresh: () -> Unit,
    refreshTrigger: Int
) {
    val context = LocalContext.current
    val files = remember(currentDir, refreshTrigger) {
        currentDir.listFiles()?.filter { it.canRead() }
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() })) ?: emptyList()
    }
    val selectedFiles = remember { mutableStateListOf<File>() }
    var selectionMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectionMode && selectedFiles.isNotEmpty())
                            "${selectedFiles.size} selected"
                        else currentDir.absolutePath,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectionMode) {
                            selectedFiles.clear()
                            selectionMode = false
                        } else {
                            (context as? Activity)?.finish()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }

                    BackHandler(enabled = selectionMode) {
                        selectionMode = false
                        selectedFiles.clear()
                    }
                },
                actions = {
                    if (selectionMode && selectedFiles.isNotEmpty()) {
                        IconButton(onClick = {
                            FileClipboard.files = selectedFiles.toList()
                            FileClipboard.isCut = false
                            selectedFiles.clear()
                            selectionMode = false
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                        IconButton(onClick = {
                            FileClipboard.files = selectedFiles.toList()
                            FileClipboard.isCut = true
                            selectedFiles.clear()
                            selectionMode = false
                        }) {
                            Icon(Icons.Default.ContentCut, contentDescription = "Cut")
                        }
                        IconButton(onClick = {
                            selectedFiles.forEach {
                                deleteFile(it)
                            }
                            selectedFiles.clear()
                            selectionMode = false
                            onRefresh()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    if (FileClipboard.hasItems) {
                        IconButton(onClick = {
                            FileClipboard.files.forEach { file ->
                                val target = File(currentDir, file.name)
                                if (file.absolutePath != target.absolutePath) {
                                    try {
                                        if (file.isDirectory) {
                                            file.copyRecursively(target, overwrite = true)
                                        } else {
                                            file.copyTo(target, overwrite = true)
                                        }
                                        if (FileClipboard.isCut) file.deleteRecursively()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            FileClipboard.clear()
                            onRefresh()
                        }) {
                            Icon(Icons.Default.ContentPasteGo, contentDescription = "Paste")
                        }
                    }
                }
            )
        }
    ) { padding ->

                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(files) { file ->
                        val isSelected = selectedFiles.contains(file)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (selectionMode) {
                                            if (isSelected) selectedFiles.remove(file)
                                            else selectedFiles.add(file)
                                        } else {
                                            onItemClick(file)
                                        }
                                    },
                                    onLongClick = {
                                        if (!selectionMode) {
                                            selectionMode = true
                                            selectedFiles.add(file)
                                        }
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectionMode) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = {
                                        if (it) selectedFiles.add(file) else selectedFiles.remove(file)
                                    },
                                    modifier = Modifier.size(18.dp).padding(end = 10.dp)
                                )
                            }

                            if (file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")) {
                                AsyncImage(
                                    model = file,
                                    contentDescription = file.name,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(30.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = getFileIcon(file),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(30.dp)
                                )
                            }

                            Text(
                                text = file.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }

    }
}






fun openFile(context: Activity, file: File) {
    val uri: Uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".fileprovider",
        file
    )

    val extension = file.extension.lowercase()
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Unsupported File Type", Toast.LENGTH_SHORT).show()
    }
}



fun deleteFile(file: File): Boolean {
    return if (file.isDirectory) {
        file.deleteRecursively()
    } else {
        file.delete()
    }
}

@Composable
fun getFileIcon(file: File): ImageVector {
    return when {
        file.isDirectory -> Icons.Default.Folder
        file.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> Icons.Default.Image
        file.extension.lowercase() in listOf("mp3", "wav", "flac", "aac") -> Icons.Default.MusicNote
        file.extension.lowercase() in listOf("mp4", "mkv", "avi", "mov") -> Icons.Default.Movie
        file.extension.lowercase() == "pdf" -> Icons.Default.PictureAsPdf
        file.extension.lowercase() in listOf("zip", "rar", "7z") -> Icons.Default.FolderZip
        else -> Icons.Default.Description
    }
}