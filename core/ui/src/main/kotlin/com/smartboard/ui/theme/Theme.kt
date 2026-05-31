package com.smartboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.smartboard.model.ThemeAccent
import com.smartboard.model.ThemeMode

data class SmartBoardColors(
    val keyboardBackground: androidx.compose.ui.graphics.Color,
    val keySurface: androidx.compose.ui.graphics.Color,
    val keySurfaceSpecial: androidx.compose.ui.graphics.Color,
    val keySurfaceAccent: androidx.compose.ui.graphics.Color,
    val keyText: androidx.compose.ui.graphics.Color,
    val keyTextOnAccent: androidx.compose.ui.graphics.Color,
    val suggestionBar: androidx.compose.ui.graphics.Color,
    val pinnedBarBg: androidx.compose.ui.graphics.Color,
    val pinnedChipBg: androidx.compose.ui.graphics.Color,
    val pinnedChipBgActive: androidx.compose.ui.graphics.Color,
    val clipboardBg: androidx.compose.ui.graphics.Color,
    val categoryChipBg: androidx.compose.ui.graphics.Color,
    val categoryChipText: androidx.compose.ui.graphics.Color,
    val divider: androidx.compose.ui.graphics.Color,
)

val LocalSmartBoardColors = staticCompositionLocalOf {
    SmartBoardColors(
        keyboardBackground = KeyboardBackground,
        keySurface = KeySurface,
        keySurfaceSpecial = KeySurfaceSpecial,
        keySurfaceAccent = KeySurfaceAccent,
        keyText = KeyText,
        keyTextOnAccent = KeyTextOnAccent,
        suggestionBar = SuggestionBar,
        pinnedBarBg = PinnedBarBg,
        pinnedChipBg = PinnedChipBg,
        pinnedChipBgActive = PinnedChipBgActive,
        clipboardBg = ClipboardBg,
        categoryChipBg = CategoryChipBg,
        categoryChipText = CategoryChipText,
        divider = DividerMuted,
    )
}

@Suppress("DEPRECATION")
private val DmSans = FontFamily.SansSerif
private val Inter = FontFamily.SansSerif

val SmartBoardTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DmSans,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
    ),
)

fun smartBoardColors(
    themeMode: ThemeMode,
    isDarkTheme: Boolean,
    accent: ThemeAccent,
): SmartBoardColors {
    val accentColor = when (accent) {
        ThemeAccent.DEFAULT, ThemeAccent.BLUE -> KeySurfaceAccent
        ThemeAccent.PURPLE -> PurpleAccent
        ThemeAccent.GREEN -> GreenAccent
    }

    val dark = isDarkTheme || themeMode == ThemeMode.DARK
    val amoled = themeMode == ThemeMode.AMOLED && dark

    return when {
        amoled -> SmartBoardColors(
            keyboardBackground = KeyboardBackgroundAmoled,
            keySurface = KeySurfaceAmoled,
            keySurfaceSpecial = KeySurfaceSpecialAmoled,
            keySurfaceAccent = accentColor,
            keyText = KeyTextDark,
            keyTextOnAccent = KeyTextOnAccent,
            suggestionBar = KeyboardBackgroundAmoled,
            pinnedBarBg = KeyboardBackgroundAmoled,
            pinnedChipBg = KeySurfaceAmoled,
            pinnedChipBgActive = accentColor,
            clipboardBg = KeyboardBackgroundAmoled,
            categoryChipBg = CategoryChipBg.copy(alpha = 0.2f),
            categoryChipText = accentColor,
            divider = KeySurfaceDark,
        )
        dark -> SmartBoardColors(
            keyboardBackground = KeyboardBackgroundDark,
            keySurface = KeySurfaceDark,
            keySurfaceSpecial = KeySurfaceSpecialDark,
            keySurfaceAccent = accentColor,
            keyText = KeyTextDark,
            keyTextOnAccent = KeyTextOnAccent,
            suggestionBar = KeyboardBackgroundDark,
            pinnedBarBg = PinnedBarBgDark,
            pinnedChipBg = PinnedChipBgDark,
            pinnedChipBgActive = accentColor,
            clipboardBg = ClipboardBgDark,
            categoryChipBg = CategoryChipBg.copy(alpha = 0.2f),
            categoryChipText = accentColor,
            divider = KeySurfaceDark,
        )
        else -> SmartBoardColors(
            keyboardBackground = KeyboardBackground,
            keySurface = KeySurface,
            keySurfaceSpecial = KeySurfaceSpecial,
            keySurfaceAccent = accentColor,
            keyText = KeyText,
            keyTextOnAccent = KeyTextOnAccent,
            suggestionBar = SuggestionBar,
            pinnedBarBg = PinnedBarBg,
            pinnedChipBg = PinnedChipBg,
            pinnedChipBgActive = accentColor,
            clipboardBg = ClipboardBg,
            categoryChipBg = CategoryChipBg,
            categoryChipText = CategoryChipText,
            divider = DividerMuted,
        )
    }
}

@Composable
fun SmartBoardTheme(
    themeMode: ThemeMode,
    isSystemDark: Boolean,
    accent: ThemeAccent,
    content: @Composable () -> Unit,
) {
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }
    val colors = smartBoardColors(themeMode, useDark, accent)
    val material = if (useDark) {
        darkColorScheme(
            primary = colors.keySurfaceAccent,
            onPrimary = colors.keyTextOnAccent,
            surface = colors.keyboardBackground,
            onSurface = colors.keyText,
        )
    } else {
        lightColorScheme(
            primary = colors.keySurfaceAccent,
            onPrimary = colors.keyTextOnAccent,
            surface = colors.keyboardBackground,
            onSurface = colors.keyText,
        )
    }

    CompositionLocalProvider(LocalSmartBoardColors provides colors) {
        MaterialTheme(
            colorScheme = material,
            typography = SmartBoardTypography,
            content = content,
        )
    }
}

object SmartBoardThemeColors {
    val colors: SmartBoardColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSmartBoardColors.current
}
