package com.smartboard.domain.pins

import com.smartboard.domain.repository.PinsRepository
import com.smartboard.model.PinnedSnippet
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePinsUseCase @Inject constructor(
    private val repository: PinsRepository,
) {
    operator fun invoke(): Flow<List<PinnedSnippet>> = repository.observePins()
}

class SavePinUseCase @Inject constructor(
    private val repository: PinsRepository,
) {
    suspend operator fun invoke(title: String, body: String, id: Long? = null): Long =
        repository.save(title, body, id)
}

class DeletePinUseCase @Inject constructor(
    private val repository: PinsRepository,
) {
    suspend operator fun invoke(id: Long) {
        repository.delete(id)
    }
}

class ReorderPinsUseCase @Inject constructor(
    private val repository: PinsRepository,
) {
    suspend operator fun invoke(orderedIds: List<Long>) {
        repository.reorder(orderedIds)
    }
}

class MovePinToFirstUseCase @Inject constructor(
    private val repository: PinsRepository,
) {
    suspend operator fun invoke(id: Long) {
        repository.moveToFirst(id)
    }
}
