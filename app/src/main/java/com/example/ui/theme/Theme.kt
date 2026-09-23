package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ShenColorScheme = darkColorScheme(
  primary = NeonEmerald,
  onPrimary = CyberBlack,
  primaryContainer = NeonEmeraldDark,
  onPrimaryContainer = NeonEmerald,
  secondary = NeonCyan,
  onSecondary = CyberBlack,
  secondaryContainer = NeonCyanDark,
  onSecondaryContainer = NeonCyan,
  tertiary = CyberBlue,
  onTertiary = CyberBlack,
  background = CyberBlack,
  onBackground = CyberTextPrimary,
  surface = CyberDarkNavy,
  onSurface = CyberTextPrimary,
  surfaceVariant = CyberCardBg,
  onSurfaceVariant = CyberTextSecondary,
  outline = CyberCardBorder,
  error = CyberRed,
  onError = CyberBlack
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = ShenColorScheme,
    typography = Typography,
    content = content
  )
}
