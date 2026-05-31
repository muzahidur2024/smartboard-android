package com.smartboard.domain.backup

import com.smartboard.common.SmartResult
import com.smartboard.domain.repository.PinsRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ImportPinsUseCase @Inject constructor(
    private val pinsRepository: PinsRepository,
    private val json: Json,
) {
    suspend operator fun invoke(jsonText: String): SmartResult<Unit> {
        return try {
            val backup = json.decodeFromString<SmartBoardBackup>(jsonText)
            val sorted = backup.pinnedSnippets.sortedBy { it.sortOrder }
            for (snippet in sorted) {
                pinsRepository.save(snippet.title, snippet.body, id = null)
            }
            SmartResult.Success(Unit)
        } catch (e: Exception) {
            SmartResult.Error(e)
        }
    }
}
