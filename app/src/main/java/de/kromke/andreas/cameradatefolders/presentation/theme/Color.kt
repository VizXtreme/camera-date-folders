package de.kromke.andreas.cameradatefolders.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Purple10 = Color(0xFF21005D)
val Purple20 = Color(0xFF381E72)
val Purple40 = Color(0xFF6750A4)
val Purple80 = Color(0xFFD0BCFF)
val Purple90 = Color(0xFFEADDFF)
val PurpleGrey40 = Color(0xFF625B71)
val PurpleGrey80 = Color(0xFFCCC2DC)
val PurpleGrey90 = Color(0xFFE8DEF8)
val Pink40 = Color(0xFF7D5260)
val Pink80 = Color(0xFFEFB8C8)
val Pink90 = Color(0xFFFFD8E4)

// Action-specific accent colors for expressive states
val EmeraldGreen = Color(0xFF2E7D32)
val EmeraldGreenContainer = Color(0xFFA5D6A7)
val AmberWarning = Color(0xFFF57C00)
val AmberContainer = Color(0xFFFFE082)
val CrimsonError = Color(0xFFBA1A1A)
val CrimsonErrorContainer = Color(0xFFFFDAD6)

val LightColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Purple90,
    onPrimaryContainer = Purple10,
    secondary = PurpleGrey40,
    onSecondary = Color.White,
    secondaryContainer = PurpleGrey90,
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Pink40,
    onTertiary = Color.White,
    tertiaryContainer = Pink90,
    onTertiaryContainer = Color(0xFF31111D),
)

val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Purple20,
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Purple90,
    secondary = PurpleGrey80,
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = PurpleGrey90,
    tertiary = Pink80,
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Pink90,
)
