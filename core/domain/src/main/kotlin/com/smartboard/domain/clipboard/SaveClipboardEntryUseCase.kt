package com.smartboard.domain.clipboard

import com.smartboard.domain.repository.ClipboardRepository
import com.smartboard.common.SmartResult
import javax.inject.Inject

class SaveClipboardEntryUseCase @Inject constructor(
    private val repository: ClipboardRepository,
) {
    suspend operator fun invoke(rawText: String): SmartResult<Unit> =
        repository.insertFromClip(rawText)
}
