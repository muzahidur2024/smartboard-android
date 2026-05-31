package com.smartboard.domain.settings

import com.smartboard.model.KeyboardSettings
import com.smartboard.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<KeyboardSettings> = repository.observeSettings()
}

class UpdateSettingUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(transform: (KeyboardSettings) -> KeyboardSettings) {
        repository.update(transform)
    }
}
