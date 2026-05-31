package com.smartboard.domain.clipboard

import com.smartboard.model.ClipboardEntry
import com.smartboard.domain.repository.ClipboardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchClipboardUseCase @Inject constructor(
    private val repository: ClipboardRepository,
) {
    operator fun invoke(query: String): Flow<List<ClipboardEntry>> =
        repository.searchFts(query)
}
