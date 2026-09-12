package de.kromke.andreas.cameradatefolders.presentation.features.paths

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.kromke.andreas.cameradatefolders.presentation.components.ExpressiveActionButton
import de.kromke.andreas.cameradatefolders.presentation.components.ExpressiveSectionCard
import de.kromke.andreas.cameradatefolders.presentation.components.StatusBadge
import de.kromke.andreas.cameradatefolders.presentation.theme.AmberContainer
import de.kromke.andreas.cameradatefolders.presentation.theme.AmberWarning
import de.kromke.andreas.cameradatefolders.presentation.theme.CrimsonError
import de.kromke.andreas.cameradatefolders.presentation.theme.CrimsonErrorContainer
import de.kromke.andreas.cameradatefolders.presentation.theme.EmeraldGreen
import de.kromke.andreas.cameradatefolders.presentation.theme.EmeraldGreenContainer

@Composable
fun PathsScreen(
    camFolderDisplay: String?,
    camFolderValidation: String?,
    destFolderDisplay: String?,
    destFolderValidation: String?,
    isFileMode: Boolean,
    onSelectCamFolderClick: () -> Unit,
    onSelectDestFolderClick: () -> Unit,
    onRemoveDestFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Configure source and destination paths. By default, photos are sorted directly inside your camera folder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Camera Folder Card
        ExpressiveSectionCard(
            title = "Camera Source Folder",
            subtitle = "Directory containing photos to organize",
            icon = Icons.Rounded.CameraAlt,
            iconTint = MaterialTheme.colorScheme.primary
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Path Display Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Path:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = camFolderDisplay ?: "No camera folder selected (usually /DCIM/Camera)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = if (camFolderDisplay != null) FontFamily.Monospace else FontFamily.Default,
                                fontSize = 13.sp
                            ),
                            fontWeight = if (camFolderDisplay != null) FontWeight.Medium else FontWeight.Normal,
                            color = if (camFolderDisplay != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )

                        if (camFolderValidation != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            StatusBadge(
                                text = camFolderValidation,
                                icon = Icons.Rounded.Warning,
                                containerColor = CrimsonErrorContainer,
                                contentColor = CrimsonError
                            )
                        } else if (camFolderDisplay != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            StatusBadge(
                                text = "Path Verified",
                                icon = Icons.Rounded.CheckCircle,
                                containerColor = EmeraldGreenContainer,
                                contentColor = EmeraldGreen
                            )
                        }
                    }
                }

                // Select Button
                ExpressiveActionButton(
                    onClick = onSelectCamFolderClick,
                    text = if (camFolderDisplay != null) "Change Camera Folder" else "Select Camera Folder",
                    icon = Icons.Rounded.FolderOpen,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Arrow Flow Indicator
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowDownward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sorts or backs up into",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Destination Folder Card
        ExpressiveSectionCard(
            title = "Destination Folder (Optional)",
            subtitle = "Optional folder for backups or external storage",
            icon = Icons.Rounded.DriveFileMove,
            iconTint = MaterialTheme.colorScheme.secondary
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Path Display Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Path:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = destFolderDisplay ?: "In-place inside Camera Folder (Default)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = if (destFolderDisplay != null) FontFamily.Monospace else FontFamily.Default,
                                fontSize = 13.sp
                            ),
                            fontWeight = if (destFolderDisplay != null) FontWeight.Medium else FontWeight.Normal,
                            color = if (destFolderDisplay != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )

                        if (destFolderValidation != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            StatusBadge(
                                text = destFolderValidation,
                                icon = Icons.Rounded.Warning,
                                containerColor = CrimsonErrorContainer,
                                contentColor = CrimsonError
                            )
                        } else if (destFolderDisplay != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            StatusBadge(
                                text = "Path Verified",
                                icon = Icons.Rounded.CheckCircle,
                                containerColor = EmeraldGreenContainer,
                                contentColor = EmeraldGreen
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpressiveActionButton(
                        onClick = onSelectDestFolderClick,
                        text = if (destFolderDisplay != null) "Change Destination" else "Select Destination",
                        icon = Icons.Rounded.CreateNewFolder,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    if (destFolderDisplay != null) {
                        OutlinedButton(
                            onClick = onRemoveDestFolderClick,
                            shape = CircleShape,
                            modifier = Modifier.height(52.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Clear,
                                contentDescription = "Remove Destination",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Remove",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
