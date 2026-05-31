package com.smartboard.domain.repository

import com.smartboard.model.KeyboardSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeSettings(): Flow<KeyboardSettings>
    suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings)
}
