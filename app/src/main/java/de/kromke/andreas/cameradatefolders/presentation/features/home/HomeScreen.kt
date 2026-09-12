package de.kromke.andreas.cameradatefolders.presentation.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.LayersClear
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.kromke.andreas.cameradatefolders.presentation.components.ExpressiveActionButton
import de.kromke.andreas.cameradatefolders.presentation.components.StatusBadge
import de.kromke.andreas.cameradatefolders.presentation.theme.AmberContainer
import de.kromke.andreas.cameradatefolders.presentation.theme.AmberWarning
import de.kromke.andreas.cameradatefolders.presentation.theme.CrimsonError
import de.kromke.andreas.cameradatefolders.presentation.theme.CrimsonErrorContainer
import de.kromke.andreas.cameradatefolders.presentation.theme.EmeraldGreen
import de.kromke.andreas.cameradatefolders.presentation.theme.EmeraldGreenContainer

@Composable
fun HomeScreen(
    logText: String,
    isRunningSort: Boolean,
    isRunningRevert: Boolean,
    camFolderUri: String?,
    destFolderUri: String?,
    isBackupCopy: Boolean,
    isDryRun: Boolean,
    isFileMode: Boolean,
    onStartClick: () -> Unit,
    onRevertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isAnyRunning = isRunningSort || isRunningRevert
    val scrollState = rememberScrollState()

    // Auto-scroll to end when new log arrives
    LaunchedEffect(logText) {
        if (logText.isNotEmpty()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status badges strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mode Badge
            if (isFileMode) {
                StatusBadge(
                    text = "File Mode (Fast)",
                    icon = Icons.Rounded.Speed,
                    containerColor = EmeraldGreenContainer,
                    contentColor = EmeraldGreen
                )
            } else {
                StatusBadge(
                    text = "SAF Mode",
                    icon = Icons.Rounded.LinearScale,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            // Backup badge
            if (isBackupCopy) {
                StatusBadge(
                    text = "Backup Copy",
                    icon = Icons.Rounded.Backup,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            // Dry run badge
            if (isDryRun) {
                StatusBadge(
                    text = "Dry Run (Simulation)",
                    icon = Icons.Rounded.Warning,
                    containerColor = AmberContainer,
                    contentColor = AmberWarning
                )
            }

            // Camera folder selected indicator
            if (camFolderUri != null) {
                StatusBadge(
                    text = "Source Set",
                    icon = Icons.Rounded.CheckCircle,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            } else {
                StatusBadge(
                    text = "No Camera Folder",
                    icon = Icons.Rounded.Warning,
                    containerColor = CrimsonErrorContainer,
                    contentColor = CrimsonError
                )
            }
        }

        // Terminal / Log Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header of log card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Operation Log",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (isAnyRunning) {
                        StatusBadge(
                            text = "Running...",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        StatusBadge(
                            text = "Ready",
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Progress Indicator when running
                AnimatedVisibility(visible = isAnyRunning) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Scrollable log text body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = logText.ifEmpty { "Ready. Select an action below to begin." },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = if (logText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        )
                    }
                }
            }
        }

        // Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Primary Action Button (SORT / BACKUP / STOP)
            val startButtonText = when {
                isRunningSort -> "STOP"
                isBackupCopy -> "BACKUP"
                else -> "SORT"
            }
            val startButtonIcon = when {
                isRunningSort -> Icons.Rounded.Stop
                isBackupCopy -> Icons.Rounded.Backup
                else -> Icons.Rounded.Sort
            }
            val startButtonColors = when {
                isRunningSort -> ButtonDefaults.buttonColors(
                    containerColor = CrimsonError,
                    contentColor = Color.White
                )
                else -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }

            ExpressiveActionButton(
                onClick = onStartClick,
                text = startButtonText,
                icon = startButtonIcon,
                enabled = !isRunningRevert,
                colors = startButtonColors,
                modifier = Modifier.weight(1f)
            )

            // Secondary Action Button (REVERT / FLATTEN / STOP)
            val revertButtonText = when {
                isRunningRevert -> "STOP"
                isBackupCopy -> "FLATTEN"
                else -> "REVERT"
            }
            val revertButtonIcon = when {
                isRunningRevert -> Icons.Rounded.Stop
                isBackupCopy -> Icons.Rounded.LayersClear
                else -> Icons.Rounded.Restore
            }
            val revertButtonColors = when {
                isRunningRevert -> ButtonDefaults.buttonColors(
                    containerColor = CrimsonError,
                    contentColor = Color.White
                )
                else -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            ExpressiveActionButton(
                onClick = onRevertClick,
                text = revertButtonText,
                icon = revertButtonIcon,
                enabled = !isRunningSort,
                colors = revertButtonColors,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
