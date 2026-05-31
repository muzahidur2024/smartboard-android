package com.smartboard.domain.clipboard

import com.smartboard.model.ClipboardCategory
import com.smartboard.model.ClipboardEntry
import com.smartboard.domain.repository.ClipboardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveClipboardUseCase @Inject constructor(
    private val repository: ClipboardRepository,
) {
    operator fun invoke(): Flow<List<ClipboardEntry>> = repository.observeAll()
}

class ObserveClipboardFilteredUseCase @Inject constructor(
    private val repository: ClipboardRepository,
) {
    operator fun invoke(category: ClipboardCategory?): Flow<List<ClipboardEntry>> =
        repository.observeFiltered(category)
}
