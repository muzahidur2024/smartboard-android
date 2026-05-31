package com.smartboard.domain.repository

import com.smartboard.model.PinnedSnippet
import kotlinx.coroutines.flow.Flow

interface PinsRepository {
    fun observePins(): Flow<List<PinnedSnippet>>
    suspend fun save(title: String, body: String, id: Long? = null): Long
    suspend fun delete(id: Long)
    suspend fun reorder(orderedIds: List<Long>)
    suspend fun moveToFirst(id: Long)
}
