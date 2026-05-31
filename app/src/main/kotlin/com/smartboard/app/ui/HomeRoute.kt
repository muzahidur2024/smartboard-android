package com.smartboard.app.ui

import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun HomeRoute(
    onOpenSettings: () -> Unit,
    onOpenOnboarding: () -> Unit,
) {
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(id = com.smartboard.app.R.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Button(onClick = {
            ctx.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }, modifier = Modifier.fillMaxWidth()) {
            Text(ctx.getString(com.smartboard.app.R.string.enable_keyboard))
        }
        Button(onClick = {
            val imm = ctx.getSystemService(InputMethodManager::class.java)
            imm?.showInputMethodPicker()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(ctx.getString(com.smartboard.app.R.string.choose_default))
        }
        Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
            Text(ctx.getString(com.smartboard.app.R.string.open_settings))
        }
        Button(onClick = onOpenOnboarding, modifier = Modifier.fillMaxWidth()) {
            Text(ctx.getString(com.smartboard.app.R.string.open_onboarding))
        }
    }
}
