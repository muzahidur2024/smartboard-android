package com.smartboard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.smartboard.app.navigation.AppNavGraph
import com.smartboard.domain.settings.ObserveSettingsUseCase
import com.smartboard.domain.settings.UpdateSettingUseCase
import com.smartboard.model.ThemeAccent
import com.smartboard.model.ThemeMode
import com.smartboard.ui.theme.SmartBoardTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var observeSettings: ObserveSettingsUseCase

    @Inject
    lateinit var updateSetting: UpdateSettingUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by observeSettings().collectAsState(initial = null)
            val dark = isSystemInDarkTheme()
            SmartBoardTheme(
                themeMode = settings?.themeMode ?: ThemeMode.SYSTEM,
                isSystemDark = dark,
                accent = settings?.themeAccent ?: ThemeAccent.DEFAULT,
            ) {
                AppNavGraph(
                    settings = settings,
                    onOnboardingDone = {
                        lifecycleScope.launch {
                            updateSetting { it.copy(onboardingComplete = true) }
                        }
                    },
                )
            }
        }
    }
}
