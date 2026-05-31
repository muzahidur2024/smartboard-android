package com.smartboard.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartboard.model.KeyboardSettings
import com.smartboard.onboarding.OnboardingNavGraph
import com.smartboard.settings.SettingsNavGraph

object Routes {
    const val HOME = "home"
    const val ONBOARDING = "onboarding"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavGraph(
    settings: KeyboardSettings?,
    onOnboardingDone: () -> Unit,
) {
    val nav = rememberNavController()
    LaunchedEffect(settings?.onboardingComplete) {
        if (settings != null && !settings.onboardingComplete) {
            nav.navigate(Routes.ONBOARDING) {
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            com.smartboard.app.ui.HomeRoute(
                onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                onOpenOnboarding = { nav.navigate(Routes.ONBOARDING) },
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingNavGraph(
                onFinished = {
                    onOnboardingDone()
                    nav.popBackStack(Routes.HOME, false)
                },
                onSkip = {
                    onOnboardingDone()
                    nav.popBackStack(Routes.HOME, false)
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsNavGraph(onBack = { nav.popBackStack() })
        }
    }
}
