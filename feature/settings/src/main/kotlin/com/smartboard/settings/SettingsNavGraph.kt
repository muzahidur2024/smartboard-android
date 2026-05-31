package com.smartboard.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartboard.model.ThemeAccent
import com.smartboard.model.ThemeMode
import com.smartboard.ui.components.SettingsToggleRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNavGraph(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ROUTE_MAIN) {
        composable(ROUTE_MAIN) {
            MainSettingsScreen(
                viewModel = viewModel,
                onBack = onBack,
                onLanguages = { navController.navigate(ROUTE_LANGUAGES) },
            )
        }
        composable(ROUTE_LANGUAGES) {
            LanguagesSettingsScreen(
                onBack = { navController.popBackStack() },
                viewModel = viewModel,
            )
        }
    }
}

private const val ROUTE_MAIN = "main"
private const val ROUTE_LANGUAGES = "languages"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onLanguages: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        val s = settings ?: return@Scaffold
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SectionCard(title = stringResource(R.string.settings_appearance_section)) {
                Text(
                    stringResource(R.string.settings_theme_label),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = s.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            label = { Text(themeLabel(mode)) },
                        )
                    }
                }
                Text(
                    stringResource(R.string.settings_accent_label),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    ThemeAccent.entries.forEach { accent ->
                        AccentSwatch(
                            color = accentColor(accent),
                            selected = s.themeAccent == accent,
                            onClick = { viewModel.setThemeAccent(accent) },
                        )
                    }
                }
                Text(
                    stringResource(R.string.settings_height_label),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                Slider(
                    value = s.keyboardHeightScale,
                    onValueChange = { viewModel.setKeyboardHeight(it) },
                    valueRange = 0.7f..1.4f,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            SectionCard(title = stringResource(R.string.settings_languages_section)) {
                TextButton(
                    onClick = onLanguages,
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    Text(stringResource(R.string.settings_languages_manage))
                }
            }

            SectionCard(title = stringResource(R.string.settings_keyboard_section)) {
                SettingsToggleRow(
                    title = stringResource(R.string.settings_number_row),
                    checked = s.numberRowEnabled,
                    onCheckedChange = { viewModel.setNumberRow(it) },
                )
                SettingsToggleRow(
                    title = stringResource(R.string.settings_haptics),
                    checked = s.hapticEnabled,
                    onCheckedChange = { viewModel.setHaptics(it) },
                )
                SettingsToggleRow(
                    title = stringResource(R.string.settings_sound),
                    checked = s.soundEnabled,
                    onCheckedChange = { viewModel.setSound(it) },
                )
                SettingsToggleRow(
                    title = stringResource(R.string.settings_key_border),
                    checked = s.keyBorderOutlined,
                    onCheckedChange = { viewModel.setKeyBorderOutlined(it) },
                )
                SettingsToggleRow(
                    title = stringResource(R.string.settings_pin_bar),
                    checked = s.pinnedBarVisible,
                    onCheckedChange = { viewModel.setPinnedBarVisible(it) },
                )
            }

            SectionCard(title = stringResource(R.string.settings_typing_section)) {
                SettingsToggleRow(
                    title = stringResource(R.string.settings_word_suggestions),
                    checked = s.wordSuggestionsEnabled,
                    onCheckedChange = { viewModel.setWordSuggestions(it) },
                )
                SettingsToggleRow(
                    title = stringResource(R.string.settings_autocorrect),
                    checked = s.autocorrectEnabled,
                    onCheckedChange = { viewModel.setAutocorrect(it) },
                )
            }

            SectionCard(title = stringResource(R.string.settings_clipboard_section)) {
                SettingsToggleRow(
                    title = stringResource(R.string.settings_clipboard_enabled),
                    checked = s.clipboardEnabled,
                    onCheckedChange = { viewModel.setClipboardEnabled(it) },
                )
                SettingsToggleRow(
                    title = stringResource(R.string.settings_auto_categorize),
                    checked = s.autoCategorize,
                    onCheckedChange = { viewModel.setAutoCategorize(it) },
                )
                SettingsToggleRow(
                    title = stringResource(R.string.settings_gif_network),
                    checked = s.networkGifEnabled,
                    onCheckedChange = { viewModel.setGifNetwork(it) },
                )
            }

            Spacer(Modifier.size(8.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            content()
        }
    }
}

@Composable
private fun AccentSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 0.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape,
            )
            .clickable { onClick() },
    )
}

@Composable
private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
    ThemeMode.DARK -> stringResource(R.string.theme_dark)
    ThemeMode.AMOLED -> stringResource(R.string.theme_amoled)
    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
}

private fun accentColor(accent: ThemeAccent): Color = when (accent) {
    ThemeAccent.DEFAULT, ThemeAccent.BLUE -> Color(0xFF4285F4)
    ThemeAccent.PURPLE -> Color(0xFF9C27B0)
    ThemeAccent.GREEN -> Color(0xFF34A853)
}
