package de.kromke.andreas.cameradatefolders.presentation.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class AppDestination(
    val title: String,
    val icon: ImageVector
) {
    HOME("Action", Icons.Rounded.PlayCircle),
    PATHS("Paths", Icons.Rounded.FolderCopy),
    PREFERENCES("Preferences", Icons.Rounded.Tune),
    ABOUT("About", Icons.Rounded.Info)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    currentDestination: AppDestination,
    onDestinationSelected: (AppDestination) -> Unit,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600

    if (isExpanded) {
        // Expanded layout (Tablets / Landscape): NavigationRail on left
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                AppDestination.values().forEach { destination ->
                    NavigationRailItem(
                        selected = currentDestination == destination,
                        onClick = { onDestinationSelected(destination) },
                        icon = { Icon(destination.icon, contentDescription = destination.title) },
                        label = { Text(destination.title) }
                    )
                }
            }

            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = currentDestination.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    content()
                }
            }
        }
    } else {
        // Compact layout (Phones): NavigationBar on bottom
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = currentDestination.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    AppDestination.values().forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination == destination,
                            onClick = { onDestinationSelected(destination) },
                            icon = { Icon(destination.icon, contentDescription = destination.title) },
                            label = { Text(destination.title) }
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                content()
            }
        }
    }
}
