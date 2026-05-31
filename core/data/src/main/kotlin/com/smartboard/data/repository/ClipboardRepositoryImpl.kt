package com.smartboard.data.repository

import com.smartboard.common.SmartBoardDispatchers
import com.smartboard.common.SmartResult
import com.smartboard.common.TimeProvider
import com.smartboard.data.classifier.ClipboardClassifier
import com.smartboard.data.db.ClipboardDao
import com.smartboard.data.mapper.toDomain
import com.smartboard.data.datastore.SettingsDataStore
import com.smartboard.domain.repository.ClipboardRepository
import com.smartboard.model.ClipboardCategory
import com.smartboard.model.ClipboardEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepositoryImpl @Inject constructor(
    private val clipboardDao: ClipboardDao,
    private val classifier: ClipboardClassifier,
    private val dispatchers: SmartBoardDispatchers,
    private val timeProvider: TimeProvider,
    private val settingsDataStore: SettingsDataStore,
) : ClipboardRepository {

    override fun observeAll(): Flow<List<ClipboardEntry>> =
        clipboardDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeFiltered(category: ClipboardCategory?): Flow<List<ClipboardEntry>> {
        val catName = category?.takeUnless { it == ClipboardCategory.OTHER }?.name
        return clipboardDao.observeFiltered(catName).map { list -> list.map { it.toDomain() } }
    }

    override fun searchFts(query: String): Flow<List<ClipboardEntry>> {
        val q = query.trim()
        if (q.isEmpty()) return observeAll()
        val match = buildFtsQuery(q)
        return clipboardDao.searchMatch(match).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertFromClip(rawText: String): SmartResult<Unit> = withContext(dispatchers.io) {
        try {
            val settings = settingsDataStore.settingsFlow.first()
            if (!settings.clipboardEnabled) return@withContext SmartResult.Success(Unit)

            val text = rawText.trim()
            if (text.isEmpty()) return@withContext SmartResult.Success(Unit)

            val hash = sha256(text)
            val existing = clipboardDao.getLatestByHash(hash)
            if (existing != null && existing.contentText == text) {
                return@withContext SmartResult.Success(Unit)
            }

            val category = if (settings.autoCategorize) {
                classifier.classify(text)
            } else {
                ClipboardCategory.PLAIN
            }

            val now = timeProvider.currentTimeMillis()
            val entity = com.smartboard.data.db.entities.ClipboardEntryEntity(
                contentText = text,
                contentHash = hash,
                category = category.name,
                isPinned = false,
                isFavorite = false,
                createdAtEpochMs = now,
                lastUsedAtEpochMs = now,
                usageCount = 0,
            )
            clipboardDao.insert(entity)
            clipboardDao.trimOldestNonPinned(settings.clipboardMaxEntries)
            SmartResult.Success(Unit)
        } catch (e: Exception) {
            SmartResult.Error(e)
        }
    }

    override suspend fun deleteById(id: Long) = withContext(dispatchers.io) {
        clipboardDao.deleteById(id)
    }

    override suspend fun clearAll() = withContext(dispatchers.io) {
        clipboardDao.deleteAll()
    }

    override suspend fun setPinned(id: Long, pinned: Boolean) = withContext(dispatchers.io) {
        val row = clipboardDao.getById(id) ?: return@withContext
        clipboardDao.update(row.copy(isPinned = pinned))
    }

    override suspend fun incrementUsage(id: Long) = withContext(dispatchers.io) {
        val row = clipboardDao.getById(id) ?: return@withContext
        val now = timeProvider.currentTimeMillis()
        clipboardDao.update(
            row.copy(
                lastUsedAtEpochMs = now,
                usageCount = row.usageCount + 1,
            ),
        )
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun buildFtsQuery(userQuery: String): String {
        val tokens = userQuery.split(Regex("\\s+"))
            .map { it.replace("\"", "").trim() }
            .filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return "\"\""
        return tokens.joinToString(" AND ") { token ->
            val safe = token.replace("*", "")
            "\"${safe}\"*"
        }
    }
}
