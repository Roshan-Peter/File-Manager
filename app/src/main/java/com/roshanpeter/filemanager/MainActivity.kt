package com.roshanpeter.filemanager

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.roshanpeter.filemanager.config.HomeScreen
import com.roshanpeter.filemanager.pages.FileExplorerActivity
import com.roshanpeter.filemanager.ui.theme.FileManagerTheme
import androidx.core.net.toUri
import java.io.File

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            FileManagerTheme {

                if (!hasAllFilesPermission(this)) {
                    showPermissionDialog(this)
                }


                    HomeScreen(context = context) { selectedDisk ->
                        val intent = Intent(context, FileExplorerActivity::class.java)
                        intent.putExtra("folder_path", selectedDisk.absolutePath)
                        context.startActivity(intent)
                    }



            }
        }
    }
}



@RequiresApi(Build.VERSION_CODES.R)
private fun hasAllFilesPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

private fun showPermissionDialog(activity: Activity) {
    AlertDialog.Builder(activity)
        .setTitle("Permission Required")
        .setMessage("To explore all files, please allow 'All Files Access' in settings.")
        .setPositiveButton("Allow") { _, _ ->
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        }
        .setNegativeButton("Cancel", null)
        .show()
}
