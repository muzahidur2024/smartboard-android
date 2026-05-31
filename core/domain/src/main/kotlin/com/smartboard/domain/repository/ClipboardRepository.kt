package com.smartboard.domain.repository

import com.smartboard.model.ClipboardCategory
import com.smartboard.model.ClipboardEntry
import com.smartboard.common.SmartResult
import kotlinx.coroutines.flow.Flow

interface ClipboardRepository {
    fun observeAll(): Flow<List<ClipboardEntry>>
    fun observeFiltered(category: ClipboardCategory?): Flow<List<ClipboardEntry>>
    fun searchFts(query: String): Flow<List<ClipboardEntry>>
    suspend fun insertFromClip(rawText: String): SmartResult<Unit>
    suspend fun deleteById(id: Long)
    suspend fun clearAll()
    suspend fun setPinned(id: Long, pinned: Boolean)
    suspend fun incrementUsage(id: Long)
}
