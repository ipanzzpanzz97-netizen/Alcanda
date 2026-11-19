package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkNovaColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = OnCyanDark,
    primaryContainer = CyanContainerDark,
    onPrimaryContainer = ElectricCyan,
    secondary = EmeraldNeon,
    onSecondary = Color(0xFF002411),
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = EmeraldNeon,
    tertiary = AmberPulse,
    onTertiary = Color(0xFF281A00),
    tertiaryContainer = AmberContainerDark,
    onTertiaryContainer = AmberPulse,
    background = ObsidianBg,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = GraphiteCard,
    onSurfaceVariant = TextSecondaryDark,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = GraphiteCard,
    surfaceContainerHigh = GraphiteCardHigh,
    outline = BorderSlate,
    outlineVariant = BorderSlate.copy(alpha = 0.6f),
    error = CoralError,
    onError = Color.White,
    errorContainer = CoralContainerDark,
    onErrorContainer = CoralError
)

private val LightNovaColorScheme = lightColorScheme(
    primary = CyanPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCBEFFF),
    onPrimaryContainer = Color(0xFF002530),
    secondary = EmeraldSecondaryLight,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8F7DF),
    onSecondaryContainer = Color(0xFF002914),
    tertiary = AmberTertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE5B8),
    onTertiaryContainer = Color(0xFF2B1800),
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondaryLight,
    surfaceContainerLow = LightSurface,
    surfaceContainer = LightCard,
    surfaceContainerHigh = Color(0xFFDCE7F3),
    outline = LightBorder,
    outlineVariant = LightBorder.copy(alpha = 0.7f),
    error = Color(0xFFD32F2F),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

val NovaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun NovaCoreTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkNovaColorScheme else LightNovaColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NovaTypography,
        shapes = NovaShapes,
        content = content
    )
}
