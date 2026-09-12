package de.kromke.andreas.cameradatefolders.presentation.features.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.DriveFileRenameOutline
import androidx.compose.material.icons.rounded.FolderSpecial
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.kromke.andreas.cameradatefolders.presentation.components.ExpressiveSectionCard
import de.kromke.andreas.cameradatefolders.presentation.components.SettingRadioRow
import de.kromke.andreas.cameradatefolders.presentation.components.SettingSwitchRow

data class SchemeOption(
    val id: String,
    val label: String,
    val compactPreview: String,
    val standardPreview: String
)

@Composable
fun PreferencesScreen(
    folderScheme: String,
    compactFolderNames: Boolean,
    backupCopy: Boolean,
    fullFileAccess: Boolean,
    forceFileMode: Boolean,
    prefixMode: Int,
    dryRun: Boolean,
    skipTidy: Boolean,
    isAndroid11Plus: Boolean,
    onSchemeChange: (String) -> Unit,
    onCompactFolderNamesChange: (Boolean) -> Unit,
    onBackupCopyChange: (Boolean) -> Unit,
    onFullFileAccessToggle: () -> Unit,
    onForceFileModeChange: (Boolean) -> Unit,
    onPrefixModeChange: (Int) -> Unit,
    onDryRunChange: (Boolean) -> Unit,
    onSkipTidyChange: (Boolean) -> Unit,
    onResetPreferencesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val schemes = remember {
        listOf(
            SchemeOption("ymd", "Year / Month / Day", "1965/07/21", "1965/1965-07/1965-07-21"),
            SchemeOption("md", "Month / Day", "1965-07/21", "1965-07/1965-07-21"),
            SchemeOption("yd", "Year / Day", "1965/07-21", "1965/1965-07-21"),
            SchemeOption("ym", "Year / Month", "1965/07", "1965/1965-07"),
            SchemeOption("d", "Day only", "1965-07-21", "1965-07-21"),
            SchemeOption("m", "Month only", "1965-07", "1965-07"),
            SchemeOption("y", "Year only", "1965", "1965")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Folder Organization Section
        ExpressiveSectionCard(
            title = "Subfolder Scheme",
            subtitle = "Choose how photos are grouped into date folders",
            icon = Icons.Rounded.FolderSpecial,
            iconTint = MaterialTheme.colorScheme.primary
        ) {
            Column {
                schemes.forEach { scheme ->
                    val preview = if (compactFolderNames) scheme.compactPreview else scheme.standardPreview
                    SettingRadioRow(
                        title = preview,
                        subtitle = scheme.label,
                        selected = folderScheme == scheme.id,
                        onClick = { onSchemeChange(scheme.id) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                SettingSwitchRow(
                    title = "Compact Folder Names",
                    subtitle = if (compactFolderNames) "Enabled: e.g. 1965/07/21" else "Disabled: e.g. 1965/1965-07/1965-07-21",
                    checked = compactFolderNames,
                    onCheckedChange = onCompactFolderNamesChange
                )
            }
        }

        // File Name Prefix Section
        ExpressiveSectionCard(
            title = "File Name Prefix Handling",
            subtitle = "Manage filename prefixes such as 'PXL_'",
            icon = Icons.Rounded.DriveFileRenameOutline,
            iconTint = MaterialTheme.colorScheme.secondary
        ) {
            Column {
                SettingRadioRow(
                    title = "Leave as is",
                    subtitle = "Do not modify file name prefixes (e.g. PXL_20230101.jpg)",
                    selected = prefixMode == 0,
                    onClick = { onPrefixModeChange(0) }
                )
                SettingRadioRow(
                    title = "Move to End of File Name",
                    subtitle = "Move prefix to end (e.g. 20230101_PXL.jpg)",
                    selected = prefixMode == 1,
                    onClick = { onPrefixModeChange(1) }
                )
                SettingRadioRow(
                    title = "Remove Prefix",
                    subtitle = "Completely remove prefix (e.g. 20230101.jpg)",
                    selected = prefixMode == 2,
                    onClick = { onPrefixModeChange(2) }
                )
            }
        }

        // Storage & Performance Section
        ExpressiveSectionCard(
            title = "Storage & Performance",
            subtitle = "Engine settings and file system access",
            icon = Icons.Rounded.Speed,
            iconTint = MaterialTheme.colorScheme.tertiary
        ) {
            Column {
                SettingSwitchRow(
                    title = "Allow Full File Access",
                    subtitle = if (isAndroid11Plus) "Direct storage access (~100x faster than SAF)" else "Requires Android 11 or higher",
                    checked = fullFileAccess,
                    onCheckedChange = { onFullFileAccessToggle() },
                    enabled = isAndroid11Plus
                )

                SettingSwitchRow(
                    title = if (fullFileAccess) "Use File Mode (~100 times faster)" else "Force File Mode",
                    subtitle = "Bypasses Storage Access Framework when permitted",
                    checked = forceFileMode,
                    onCheckedChange = onForceFileModeChange
                )

                SettingSwitchRow(
                    title = "Make Copies (Backup)",
                    subtitle = "Copy photos to destination folder instead of moving original files",
                    checked = backupCopy,
                    onCheckedChange = onBackupCopyChange
                )
            }
        }

        // Diagnostics & Debug Section
        ExpressiveSectionCard(
            title = "Debug & Diagnostics",
            subtitle = "Simulation and maintenance controls",
            icon = Icons.Rounded.BugReport,
            iconTint = MaterialTheme.colorScheme.error
        ) {
            Column {
                SettingSwitchRow(
                    title = "Dry Run (Simulation)",
                    subtitle = "Simulate all operations without moving or creating files",
                    checked = dryRun,
                    onCheckedChange = onDryRunChange
                )

                SettingSwitchRow(
                    title = "Skip Tidy Phase",
                    subtitle = "Do not delete empty date folders after organizing files",
                    checked = skipTidy,
                    onCheckedChange = onSkipTidyChange
                )
            }
        }

        // Reset Button
        OutlinedButton(
            onClick = onResetPreferencesClick,
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.RestartAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Reset Preferences to Defaults",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
