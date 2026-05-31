package com.smartboard.domain.clipboard

import com.smartboard.domain.repository.ClipboardRepository
import javax.inject.Inject

class PinClipboardEntryUseCase @Inject constructor(
    private val repository: ClipboardRepository,
) {
    suspend operator fun invoke(id: Long, pinned: Boolean) {
        repository.setPinned(id, pinned)
    }
}

class RecordClipboardUsageUseCase @Inject constructor(
    private val repository: ClipboardRepository,
) {
    suspend operator fun invoke(id: Long) {
        repository.incrementUsage(id)
    }
}
