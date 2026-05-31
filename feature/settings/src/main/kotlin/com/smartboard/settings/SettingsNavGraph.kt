package com.smartboard.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
                .padding(8.dp),
        ) {
            Text(stringResource(R.string.settings_languages_section), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = onLanguages) {
                Text(stringResource(R.string.settings_languages_manage))
            }
            Text(stringResource(R.string.settings_clipboard_section), style = MaterialTheme.typography.titleMedium)
            SettingsToggleRow(
                title = stringResource(R.string.settings_clipboard_enabled),
                checked = s.clipboardEnabled,
                onCheckedChange = { viewModel.setClipboardEnabled(it) },
            )
            SettingsToggleRow(
                title = stringResource(R.string.settings_pin_bar),
                checked = s.pinnedBarVisible,
                onCheckedChange = { viewModel.setPinnedBarVisible(it) },
            )
            SettingsToggleRow(
                title = stringResource(R.string.settings_gif_network),
                checked = s.networkGifEnabled,
                onCheckedChange = { viewModel.setGifNetwork(it) },
            )
            Text(stringResource(R.string.settings_keyboard_section), style = MaterialTheme.typography.titleMedium)
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
        }
    }
}
