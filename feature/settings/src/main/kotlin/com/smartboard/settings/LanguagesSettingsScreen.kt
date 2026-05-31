package com.smartboard.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smartboard.ime.layouts.json.LanguageMeta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesSettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel,
) {
    val settings by viewModel.settings.collectAsState()
    val catalog by viewModel.languageCatalog.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    val s = settings ?: return
    val metaByLocale = remember(catalog) { catalog.associateBy { it.locale } }

    if (showAdd) {
        val available = catalog.filter { it.locale !in s.activeLanguages }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(stringResource(R.string.languages_add_title)) },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(available, key = { _, m -> m.locale }) { _, meta ->
                        ListItem(
                            headlineContent = { Text(meta.nativeDisplayName) },
                            supportingContent = { Text(meta.displayName) },
                            leadingContent = { Text(meta.flagEmoji.ifBlank { "·" }) },
                            trailingContent = {
                                TextButton(onClick = {
                                    viewModel.addActiveLanguage(meta.locale)
                                    showAdd = false
                                }) {
                                    Text(stringResource(R.string.languages_add_button))
                                }
                            },
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAdd = false }) {
                    Text(stringResource(R.string.languages_add_dismiss))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.languages_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.languages_section_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.languages_globe_hint),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LazyColumn {
                itemsIndexed(s.activeLanguages, key = { _, loc -> loc }) { index, locale ->
                    val meta: LanguageMeta = metaByLocale[locale]
                        ?: LanguageMeta(locale, locale, locale, "ltr", "")
                    ListItem(
                        headlineContent = { Text(meta.nativeDisplayName) },
                        supportingContent = { Text(meta.displayName) },
                        leadingContent = { Text(meta.flagEmoji.ifBlank { "·" }) },
                        trailingContent = {
                            Column {
                                IconButton(
                                    onClick = { viewModel.moveActiveLanguageUp(index) },
                                    enabled = index > 0,
                                ) {
                                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null)
                                }
                                IconButton(
                                    onClick = { viewModel.moveActiveLanguageDown(index) },
                                    enabled = index < s.activeLanguages.lastIndex,
                                ) {
                                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
                                }
                                TextButton(onClick = { viewModel.removeActiveLanguage(locale) }) {
                                    Text(stringResource(R.string.languages_remove))
                                }
                            }
                        },
                    )
                }
            }
            TextButton(onClick = { showAdd = true }) {
                Text(stringResource(R.string.languages_add_language_cta))
            }
        }
    }
}
