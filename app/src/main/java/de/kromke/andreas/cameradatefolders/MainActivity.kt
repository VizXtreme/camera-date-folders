/*
 * Copyright (C) 2022-2026 Andreas Kromke, andreas.kromke@gmail.com
 *
 * This program is free software; you can redistribute it or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 */

package de.kromke.andreas.cameradatefolders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import de.kromke.andreas.cameradatefolders.presentation.features.home.HomeScreen
import de.kromke.andreas.cameradatefolders.presentation.features.info.InfoScreen
import de.kromke.andreas.cameradatefolders.presentation.features.paths.PathsScreen
import de.kromke.andreas.cameradatefolders.presentation.features.preferences.PreferencesScreen
import de.kromke.andreas.cameradatefolders.presentation.navigation.AppDestination
import de.kromke.andreas.cameradatefolders.presentation.navigation.AppShell
import de.kromke.andreas.cameradatefolders.presentation.theme.AppTheme
import java.io.File

class MainActivity : ComponentActivity() {

    private val sMaxLogLen = 10000

    private var mDcimTreeUri: Uri? = null
    private var mDestTreeUri: Uri? = null
    private var mbSafModeIsDestFolder = false

    // Observables for Compose UI
    private var currentDestination by mutableStateOf(AppDestination.HOME)
    private var logText by mutableStateOf("")
    private var isRunningSort by mutableStateOf(false)
    private var isRunningRevert by mutableStateOf(false)

    // Preference states
    private var folderScheme by mutableStateOf("ymd")
    private var compactFolderNames by mutableStateOf(false)
    private var backupCopy by mutableStateOf(false)
    private var fullFileAccess by mutableStateOf(false)
    private var forceFileMode by mutableStateOf(false)
    private var prefixMode by mutableIntStateOf(0)
    private var dryRun by mutableStateOf(false)
    private var skipTidy by mutableStateOf(false)

    // Path display and validation states
    private var camFolderDisplay by mutableStateOf<String?>(null)
    private var camFolderValidation by mutableStateOf<String?>(null)
    private var destFolderDisplay by mutableStateOf<String?>(null)
    private var destFolderValidation by mutableStateOf<String?>(null)

    // Dialog states
    private var errorDialogMessage by mutableStateOf<String?>(null)
    private var showDestFolderDialog by mutableStateOf(false)
    private var showPlayStoreDialog by mutableStateOf(false)
    private var showResetConfirmDialog by mutableStateOf(false)

    // Activity Result Launchers
    private lateinit var storageAccessPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var directorySelectLauncher: ActivityResultLauncher<Intent>
    private lateinit var legacyStoragePermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        StatusAndPrefs.Init(applicationContext)

        // Initialize URI objects from prefs
        if (StatusAndPrefs.mCamFolder != null) {
            mDcimTreeUri = Uri.parse(StatusAndPrefs.mCamFolder)
        }
        if (StatusAndPrefs.mDestFolder != null) {
            mDestTreeUri = Uri.parse(StatusAndPrefs.mDestFolder)
        }

        registerCallbacks()
        syncStateFromPrefs()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            requestLegacyPermission()
        }

        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppShell(
                        currentDestination = currentDestination,
                        onDestinationSelected = { currentDestination = it }
                    ) {
                        AnimatedContent(
                            targetState = currentDestination,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition"
                        ) { destination ->
                            when (destination) {
                                AppDestination.HOME -> HomeScreen(
                                    logText = logText,
                                    isRunningSort = isRunningSort,
                                    isRunningRevert = isRunningRevert,
                                    camFolderUri = StatusAndPrefs.mCamFolder,
                                    destFolderUri = StatusAndPrefs.mDestFolder,
                                    isBackupCopy = backupCopy,
                                    isDryRun = dryRun,
                                    isFileMode = isEffectiveFileMode(),
                                    onStartClick = { handleStartClick() },
                                    onRevertClick = { handleRevertClick() }
                                )

                                AppDestination.PATHS -> PathsScreen(
                                    camFolderDisplay = camFolderDisplay,
                                    camFolderValidation = camFolderValidation,
                                    destFolderDisplay = destFolderDisplay,
                                    destFolderValidation = destFolderValidation,
                                    isFileMode = isEffectiveFileMode(),
                                    onSelectCamFolderClick = { selectCameraFolder() },
                                    onSelectDestFolderClick = { selectDestinationFolder() },
                                    onRemoveDestFolderClick = { removeDestinationFolder() }
                                )

                                AppDestination.PREFERENCES -> PreferencesScreen(
                                    folderScheme = folderScheme,
                                    compactFolderNames = compactFolderNames,
                                    backupCopy = backupCopy,
                                    fullFileAccess = fullFileAccess,
                                    forceFileMode = forceFileMode,
                                    prefixMode = prefixMode,
                                    dryRun = dryRun,
                                    skipTidy = skipTidy,
                                    isAndroid11Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R,
                                    onSchemeChange = { newScheme ->
                                        folderScheme = newScheme
                                        StatusAndPrefs.writeValue(StatusAndPrefs.PREF_FOLDER_SCHEME, newScheme)
                                    },
                                    onCompactFolderNamesChange = { isCompact ->
                                        compactFolderNames = isCompact
                                        StatusAndPrefs.writeValue(StatusAndPrefs.PREF_COMPACT_FOLDER_NAMES, isCompact)
                                    },
                                    onBackupCopyChange = { isBackup ->
                                        backupCopy = isBackup
                                        StatusAndPrefs.writeValue(StatusAndPrefs.PREF_BACKUP_COPY, isBackup)
                                        updateInitialHomeText()
                                    },
                                    onFullFileAccessToggle = { handleFullFileAccessToggle() },
                                    onForceFileModeChange = { isForced ->
                                        forceFileMode = isForced
                                        StatusAndPrefs.writeValue(StatusAndPrefs.PREF_FORCE_FILE_MODE, isForced)
                                        updatePathDisplays()
                                    },
                                    onPrefixModeChange = { newPrefix ->
                                        prefixMode = newPrefix
                                        StatusAndPrefs.writeValue(StatusAndPrefs.PREF_PREFIX_HANDLING, newPrefix)
                                    },
                                    onDryRunChange = { isDry ->
                                        dryRun = isDry
                                        StatusAndPrefs.writeValue(StatusAndPrefs.PREF_DRY_RUN, isDry)
                                        updateInitialHomeText()
                                    },
                                    onSkipTidyChange = { skip ->
                                        skipTidy = skip
                                        StatusAndPrefs.writeValue(StatusAndPrefs.PREF_SKIP_TIDY, skip)
                                    },
                                    onResetPreferencesClick = { showResetConfirmDialog = true }
                                )

                                AppDestination.ABOUT -> {
                                    val versionInfo = Utils.getVersionInfo(this@MainActivity)
                                    val isPlayStoreVersion = BuildConfig.BUILD_TYPE == "release_play" || BuildConfig.BUILD_TYPE == "debug_play"
                                    val storeTag = if (isPlayStoreVersion) " [Play Store]" else " [free]"
                                    InfoScreen(
                                        versionName = versionInfo.versionName ?: "1.5.2",
                                        isDebug = versionInfo.isDebug,
                                        creationTime = versionInfo.strCreationTime ?: "",
                                        storeTag = storeTag,
                                        onOpenReadme = { openUrl("https://gitlab.com/AndreasK/camera-date-folders/-/blob/main/README.md") },
                                        onOpenPrivacyPolicy = { openUrl("https://gitlab.com/AndreasK/camera-date-folders/-/raw/main/privacy-policy.txt") },
                                        onOpenVersionHistory = { openUrl("https://gitlab.com/AndreasK/camera-date-folders/-/releases") }
                                    )
                                }
                            }
                        }
                    }

                    // Dialogs
                    RenderDialogs()
                }
            }
        }
    }

    @Composable
    private fun RenderDialogs() {
        // Error Dialog
        if (errorDialogMessage != null) {
            AlertDialog(
                onDismissRequest = { errorDialogMessage = null },
                icon = { Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Error") },
                text = { Text(errorDialogMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { errorDialogMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }

        // Destination Folder Dialog
        if (showDestFolderDialog) {
            AlertDialog(
                onDismissRequest = { showDestFolderDialog = false },
                icon = { Icon(Icons.Rounded.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Select Destination Folder?") },
                text = { Text(getString(R.string.str_ask_dest_folder)) },
                confirmButton = {
                    Button(onClick = {
                        showDestFolderDialog = false
                        mbSafModeIsDestFolder = true
                        directorySelectLauncher.launch(createSafPickerIntent())
                    }) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    Row {
                        if (mDestTreeUri != null) {
                            TextButton(onClick = {
                                showDestFolderDialog = false
                                removeDestinationFolder()
                            }) {
                                Text("Remove", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        TextButton(onClick = { showDestFolderDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }

        // Play Store Notice Dialog
        if (showPlayStoreDialog) {
            AlertDialog(
                onDismissRequest = { showPlayStoreDialog = false },
                icon = { Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text(getString(R.string.str_NeedManageFilesPermission)) },
                text = { Text(getString(R.string.str_NotAvailableInPlayStoreVersion)) },
                confirmButton = {
                    TextButton(onClick = { showPlayStoreDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        // Reset Preferences Confirm Dialog
        if (showResetConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                icon = { Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Reset Preferences") },
                text = { Text("Are you sure you want to reset all preferences to their default values?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetConfirmDialog = false
                            StatusAndPrefs.reset()
                            mDcimTreeUri = null
                            mDestTreeUri = null
                            syncStateFromPrefs()
                            Toast.makeText(this@MainActivity, "Preferences reset", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetConfirmDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    private fun isEffectiveFileMode(): Boolean {
        return forceFileMode || (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
    }

    private fun syncStateFromPrefs() {
        folderScheme = StatusAndPrefs.mFolderScheme ?: "ymd"
        compactFolderNames = StatusAndPrefs.mbCompactFolderNames
        backupCopy = StatusAndPrefs.mbBackupCopy
        forceFileMode = StatusAndPrefs.mbForceFileMode
        prefixMode = StatusAndPrefs.mPrefixMode
        dryRun = StatusAndPrefs.mbDryRun
        skipTidy = StatusAndPrefs.mbSkipTidy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            fullFileAccess = Environment.isExternalStorageManager()
        }

        updatePathDisplays()
        updateInitialHomeText()
    }

    private fun updateInitialHomeText() {
        if (!isRunningSort && !isRunningRevert && logText.isEmpty()) {
            val base = when {
                StatusAndPrefs.mCamFolder == null -> getString(R.string.str_no_cam_path)
                StatusAndPrefs.mbBackupCopy && StatusAndPrefs.mDestFolder == null -> getString(R.string.str_no_dest_path)
                else -> getString(if (StatusAndPrefs.mbBackupCopy) R.string.str_press_backup else R.string.str_press_start)
            }
            logText = if (StatusAndPrefs.mbDryRun && StatusAndPrefs.mCamFolder != null) {
                "$base\n\nAttention: dry run mode active!"
            } else {
                base
            }
        }
    }

    private fun updatePathDisplays() {
        val bFileMode = isEffectiveFileMode()

        // Camera folder display & validation
        if (StatusAndPrefs.mCamFolder == null) {
            camFolderDisplay = null
            camFolderValidation = null
        } else {
            val camUri = Uri.parse(StatusAndPrefs.mCamFolder)
            if (bFileMode) {
                val resolved = UriToPath.getPathFromUri(applicationContext, camUri)
                if (resolved != null) {
                    camFolderDisplay = resolved
                    val file = File(resolved)
                    camFolderValidation = when {
                        !file.isDirectory -> "Not a directory"
                        !file.canRead() -> "Not readable"
                        !file.canWrite() -> "Not writable"
                        else -> null
                    }
                } else {
                    camFolderDisplay = camUri.path?.replace("%3A", ":")?.replace("%2F", "/")
                    camFolderValidation = "Invalid path for File Mode"
                }
            } else {
                camFolderDisplay = StatusAndPrefs.mCamFolder?.replace("%3A", ":")?.replace("%2F", "/")
                camFolderValidation = null
            }
        }

        // Destination folder display & validation
        if (StatusAndPrefs.mDestFolder == null) {
            destFolderDisplay = null
            destFolderValidation = null
        } else {
            val destUri = Uri.parse(StatusAndPrefs.mDestFolder)
            if (bFileMode) {
                val resolved = UriToPath.getPathFromUri(applicationContext, destUri)
                if (resolved != null) {
                    destFolderDisplay = resolved
                    val file = File(resolved)
                    destFolderValidation = when {
                        !file.isDirectory -> "Not a directory"
                        !file.canRead() -> "Not readable"
                        !file.canWrite() -> "Not writable"
                        else -> null
                    }
                } else {
                    destFolderDisplay = destUri.path?.replace("%3A", ":")?.replace("%2F", "/")
                    destFolderValidation = "Invalid path for File Mode"
                }
            } else {
                destFolderDisplay = StatusAndPrefs.mDestFolder?.replace("%3A", ":")?.replace("%2F", "/")
                destFolderValidation = null
            }
        }
    }

    private fun registerCallbacks() {
        // Storage access permission callback (Android 11+)
        storageAccessPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val granted = Environment.isExternalStorageManager()
                StatusAndPrefs.mbFullFileAccess = granted
                fullFileAccess = granted

                if (StatusAndPrefs.mbFullFileAccess != StatusAndPrefs.mbForceFileMode) {
                    StatusAndPrefs.writeValue(StatusAndPrefs.PREF_FORCE_FILE_MODE, StatusAndPrefs.mbFullFileAccess)
                    forceFileMode = StatusAndPrefs.mbFullFileAccess
                    Toast.makeText(
                        applicationContext,
                        if (StatusAndPrefs.mbForceFileMode) R.string.str_auto_activate_file_mode else R.string.str_auto_deactivate_file_mode,
                        Toast.LENGTH_LONG
                    ).show()
                }

                if (granted) {
                    Toast.makeText(applicationContext, "Full file access granted.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, R.string.str_file_manage_permission_denied, Toast.LENGTH_LONG).show()
                }
                updatePathDisplays()
            }
        }

        // Directory select callback (SAF picker)
        directorySelectLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val treeUri = result.data?.data
                if (treeUri != null) {
                    grantUriPermission(
                        packageName,
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    contentResolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }
                pathSelectedByUser(treeUri, mbSafModeIsDestFolder)
            }
        }

        // Legacy permission launcher (Android < 11)
        legacyStoragePermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                updatePathDisplays()
            }
        }
    }

    private fun requestLegacyPermission() {
        val check = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (check != PackageManager.PERMISSION_GRANTED) {
            legacyStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun handleFullFileAccessToggle() {
        val isPlayStore = BuildConfig.BUILD_TYPE == "release_play" || BuildConfig.BUILD_TYPE == "debug_play"
        if (isPlayStore) {
            showPlayStoreDialog = true
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.fromParts("package", packageName, null)
            storageAccessPermissionLauncher.launch(intent)
        }
    }

    private fun selectCameraFolder() {
        mbSafModeIsDestFolder = false
        directorySelectLauncher.launch(createSafPickerIntent())
    }

    private fun selectDestinationFolder() {
        if (mDcimTreeUri == null) {
            Toast.makeText(applicationContext, R.string.str_must_select_dcim_path, Toast.LENGTH_LONG).show()
        } else {
            showDestFolderDialog = true
        }
    }

    private fun removeDestinationFolder() {
        StatusAndPrefs.writeValue(StatusAndPrefs.PREF_DEST_FOLDER_URI, null)
        mDestTreeUri = null
        destFolderDisplay = null
        destFolderValidation = null
        updatePathDisplays()
        updateInitialHomeText()
    }

    private fun pathSelectedByUser(selectedUri: Uri?, isDest: Boolean) {
        var treeUri = selectedUri
        var updatePrefs = false

        if (treeUri != null) {
            if (isDest) {
                if (treeUri == mDcimTreeUri) {
                    treeUri = null
                    Toast.makeText(applicationContext, R.string.str_paths_same, Toast.LENGTH_LONG).show()
                } else if (Utils.pathsOverlap(mDcimTreeUri, treeUri)) {
                    treeUri = null
                    Toast.makeText(applicationContext, R.string.str_paths_overlap, Toast.LENGTH_LONG).show()
                } else {
                    val bFileMode = isEffectiveFileMode()
                    if (bFileMode) {
                        val path = UriToPath.getPathFromUri(applicationContext, treeUri)
                        val file = if (path != null) File(path) else null
                        if (file == null || !file.canWrite()) {
                            Toast.makeText(applicationContext, R.string.str_no_file_write, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            updatePrefs = true
        }

        if (updatePrefs) {
            val valStr = treeUri?.toString()
            if (isDest) {
                mDestTreeUri = treeUri
                StatusAndPrefs.writeValue(StatusAndPrefs.PREF_DEST_FOLDER_URI, valStr)
            } else {
                mDcimTreeUri = treeUri
                StatusAndPrefs.writeValue(StatusAndPrefs.PREF_CAM_FOLDER_URI, valStr)
            }
            updatePathDisplays()
            updateInitialHomeText()
        }
    }

    private fun createSafPickerIntent(): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true)
        return intent
    }

    private fun handleStartClick() {
        if (isRunningSort || isRunningRevert) {
            if (isRunningSort) {
                appendLog("stopping...\n\n")
                stopThread()
            }
        } else {
            runThread(bFlatten = false)
        }
    }

    private fun handleRevertClick() {
        if (isRunningSort || isRunningRevert) {
            if (isRunningRevert) {
                appendLog("stopping...\n\n")
                stopThread()
            }
        } else {
            runThread(bFlatten = true)
        }
    }

    private fun runThread(bFlatten: Boolean) {
        if (StatusAndPrefs.mCamFolder == null) {
            Toast.makeText(this, "No camera folder selected!", Toast.LENGTH_LONG).show()
            return
        }

        val bFileMode = isEffectiveFileMode()
        if (bFileMode) {
            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
            if (!hasPermission) {
                Toast.makeText(this, "No permission, yet. Grant and retry!", Toast.LENGTH_LONG).show()
                return
            }
        }

        if (StatusAndPrefs.mbBackupCopy && (mDestTreeUri == null)) {
            Toast.makeText(this, "Copy (Backup) mode needs destination folder!", Toast.LENGTH_LONG).show()
            return
        }

        if (StatusAndPrefs.mbDryRun) {
            Toast.makeText(this, "Dry Run!", Toast.LENGTH_LONG).show()
        }

        val app = application as MyApplication
        val scheme = if (bFlatten) "flat" else StatusAndPrefs.mFolderScheme
        val result = app.runWorkerThread(
            this,
            mDcimTreeUri,
            mDestTreeUri,
            scheme,
            StatusAndPrefs.mbCompactFolderNames,
            StatusAndPrefs.mbBackupCopy,
            StatusAndPrefs.mbDryRun,
            bFileMode,
            StatusAndPrefs.mPrefixMode
        )

        if (result == 0) {
            logText = ""
            val initial = if (bFileMode) "in progress (File mode)...\n\n" else "in progress...\n\n"
            appendLog(initial)
            if (bFlatten) {
                isRunningRevert = true
                StatusAndPrefs.bRevertRunning = true
            } else {
                isRunningSort = true
                StatusAndPrefs.bSortRunning = true
            }
        } else {
            Toast.makeText(this, "Cannot run worker thread", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopThread() {
        val app = application as MyApplication
        app.stopWorkerThread()
    }

    private fun appendLog(text: String) {
        var updated = logText + text
        if (updated.length > sMaxLogLen) {
            updated = updated.substring(updated.length - (sMaxLogLen * 9 / 10))
        }
        logText = updated
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    /**
     * Called from WorkerThread context via MyApplication
     */
    fun messageFromThread(result: Int, result2: Int, result3: Int, text: String?, threadEnded: Boolean) {
        runOnUiThread {
            if (threadEnded) {
                StatusAndPrefs.bSortRunning = false
                StatusAndPrefs.bRevertRunning = false
                isRunningSort = false
                isRunningRevert = false

                if (result >= 0) {
                    appendLog("success:$result, failure:$result2, unchanged:$result3\n")
                } else {
                    appendLog("ERROR\n")
                    if (text != null) {
                        errorDialogMessage = text
                    }
                }
            } else if (!text.isNullOrEmpty()) {
                appendLog(text + "\n")
            }
        }
    }
}
