package com.smartboard.domain.backup

import com.smartboard.common.SmartResult
import com.smartboard.domain.repository.PinsRepository
import com.smartboard.domain.repository.SettingsRepository
import com.smartboard.common.TimeProvider
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ExportPinsUseCase @Inject constructor(
    private val pinsRepository: PinsRepository,
    private val settingsRepository: SettingsRepository,
    private val timeProvider: TimeProvider,
    private val json: Json,
) {
    suspend operator fun invoke(includeSettings: Boolean): SmartResult<String> {
        return try {
            val pins = pinsRepository.observePins().first()
            val backup = SmartBoardBackup(
                exportedAtEpochMs = timeProvider.currentTimeMillis(),
                pinnedSnippets = pins.map { it.toExport() },
                settings = if (includeSettings) {
                    settingsRepository.observeSettings().first().toExport()
                } else {
                    null
                },
            )
            SmartResult.Success(json.encodeToString(backup))
        } catch (e: Exception) {
            SmartResult.Error(e)
        }
    }
}
