package com.smartboard.data.repository

import com.smartboard.data.datastore.SettingsDataStore
import com.smartboard.domain.repository.SettingsRepository
import com.smartboard.model.KeyboardSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
) : SettingsRepository {
    override fun observeSettings(): Flow<KeyboardSettings> = dataStore.settingsFlow

    override suspend fun update(transform: (KeyboardSettings) -> KeyboardSettings) {
        dataStore.update(transform)
    }
}
