package com.example.daftar.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Teal, onPrimary = Color.White,
    primaryContainer = Mint, onPrimaryContainer = DeepTeal,
    secondary = MutedInk, onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6EDE4), onSecondaryContainer = Ink,
    tertiary = Amber, onTertiary = Color.White,
    tertiaryContainer = SoftAmber, onTertiaryContainer = Color(0xFF59340B),
    background = Cream, onBackground = Ink,
    surface = Cream, onSurface = Ink,
    surfaceVariant = Color(0xFFE7EAE1), onSurfaceVariant = MutedInk,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF5F2E9),
    surfaceContainer = Color(0xFFEFEEE4),
    surfaceContainerHigh = Color(0xFFE9E9DF),
    surfaceContainerHighest = Color(0xFFE3E5DA),
    outline = Color(0xFF77877C), outlineVariant = Color(0xFFD8DED2),
    inverseSurface = DeepTeal, inverseOnSurface = Cream,
    inversePrimary = Color(0xFF9CD8C4),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF99D5C1), onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF245046), onPrimaryContainer = Color(0xFFCDF1E3),
    secondary = Color(0xFFB9CBBE), onSecondary = Color(0xFF24382D),
    secondaryContainer = Color(0xFF334B40), onSecondaryContainer = Color(0xFFD6EBDD),
    tertiary = Color(0xFFF1C389), onTertiary = Color(0xFF472A08),
    tertiaryContainer = Color(0xFF5D401C), onTertiaryContainer = Color(0xFFFFDDB2),
    background = Color(0xFF101C18), onBackground = Color(0xFFE0E9DF),
    surface = Color(0xFF101C18), onSurface = Color(0xFFE0E9DF),
    surfaceVariant = Color(0xFF34463C), onSurfaceVariant = Color(0xFFBACABE),
    surfaceContainerLowest = Color(0xFF16251F),
    surfaceContainerLow = Color(0xFF1A2B23),
    surfaceContainer = Color(0xFF203229),
    surfaceContainerHigh = Color(0xFF293C32),
    surfaceContainerHighest = Color(0xFF32463A),
    outline = Color(0xFF879C8B), outlineVariant = Color(0xFF3B5143),
    inverseSurface = Color(0xFFE0E9DF), inverseOnSurface = DeepTeal,
    inversePrimary = Teal,
)

@Composable
fun DaftarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes(
            small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
            extraLarge = RoundedCornerShape(32.dp),
        ),
        content = content,
    )
}
