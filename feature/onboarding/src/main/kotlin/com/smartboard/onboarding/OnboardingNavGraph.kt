package com.smartboard.onboarding

import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.smartboard.onboarding.R

@Composable
fun OnboardingNavGraph(
    onFinished: () -> Unit,
    onSkip: () -> Unit,
) {
    val ctx = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.onboarding_title))
        Text(stringResource(R.string.onboarding_subtitle))
        Button(onClick = { ctx.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }) {
            Text(stringResource(R.string.onboarding_open_settings))
        }
        Button(onClick = {
            val imm = ctx.getSystemService(InputMethodManager::class.java)
            imm?.showInputMethodPicker()
        }) {
            Text(stringResource(R.string.onboarding_choose_default))
        }
        Button(onClick = onFinished) {
            Text(stringResource(R.string.onboarding_finish))
        }
        Button(onClick = onSkip) {
            Text(stringResource(R.string.onboarding_skip))
        }
    }
}
