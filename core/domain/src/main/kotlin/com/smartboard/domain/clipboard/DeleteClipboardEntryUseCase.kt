package com.smartboard.domain.clipboard

import com.smartboard.domain.repository.ClipboardRepository
import javax.inject.Inject

class DeleteClipboardEntryUseCase @Inject constructor(
    private val repository: ClipboardRepository,
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteById(id)
    }
}

class ClearClipboardHistoryUseCase @Inject constructor(
    private val repository: ClipboardRepository,
) {
    suspend operator fun invoke() {
        repository.clearAll()
    }
}
