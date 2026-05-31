package com.smartboard.data.repository

import com.smartboard.common.SmartBoardDispatchers
import com.smartboard.common.TimeProvider
import com.smartboard.data.db.PinnedSnippetDao
import com.smartboard.data.db.entities.PinnedSnippetEntity
import com.smartboard.data.mapper.toDomain
import com.smartboard.domain.repository.PinsRepository
import com.smartboard.model.PinnedSnippet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinsRepositoryImpl @Inject constructor(
    private val dao: PinnedSnippetDao,
    private val dispatchers: SmartBoardDispatchers,
    private val timeProvider: TimeProvider,
) : PinsRepository {

    override fun observePins(): Flow<List<PinnedSnippet>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun save(title: String, body: String, id: Long?): Long = withContext(dispatchers.io) {
        val now = timeProvider.currentTimeMillis()
        val trimmedTitle = title.trim().take(30)
        if (id == null) {
            val count = dao.count()
            val entity = PinnedSnippetEntity(
                title = trimmedTitle,
                body = body,
                sortOrder = count,
                createdAtEpochMs = now,
                updatedAtEpochMs = now,
            )
            return@withContext dao.insert(entity)
        } else {
            val existing = dao.getById(id) ?: return@withContext id
            dao.update(
                existing.copy(
                    title = trimmedTitle,
                    body = body,
                    updatedAtEpochMs = now,
                ),
            )
            return@withContext id
        }
    }

    override suspend fun delete(id: Long) = withContext(dispatchers.io) {
        dao.deleteById(id)
        normalizeOrder()
    }

    override suspend fun reorder(orderedIds: List<Long>) = withContext(dispatchers.io) {
        orderedIds.forEachIndexed { index, pid ->
            val row = dao.getById(pid) ?: return@forEachIndexed
            dao.update(row.copy(sortOrder = index, updatedAtEpochMs = timeProvider.currentTimeMillis()))
        }
    }

    override suspend fun moveToFirst(id: Long) = withContext(dispatchers.io) {
        val pins = dao.observeAll().first().sortedBy { it.sortOrder }.toMutableList()
        val target = pins.indexOfFirst { it.id == id }
        if (target <= 0) return@withContext
        val item = pins.removeAt(target)
        pins.add(0, item)
        pins.forEachIndexed { index, entity ->
            dao.update(entity.copy(sortOrder = index, updatedAtEpochMs = timeProvider.currentTimeMillis()))
        }
    }

    private suspend fun normalizeOrder() {
        val pins = dao.observeAll().first().sortedBy { it.sortOrder }
        pins.forEachIndexed { index, entity ->
            if (entity.sortOrder != index) {
                dao.update(entity.copy(sortOrder = index))
            }
        }
    }
}
