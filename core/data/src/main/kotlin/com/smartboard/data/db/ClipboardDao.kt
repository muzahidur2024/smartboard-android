package com.smartboard.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.smartboard.data.db.entities.ClipboardEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipboardDao {

    @Query(
        """
        SELECT * FROM clipboard_entries
        ORDER BY is_pinned DESC, created_at DESC
        """,
    )
    fun observeAll(): Flow<List<ClipboardEntryEntity>>

    @Query(
        """
        SELECT * FROM clipboard_entries
        WHERE (:category IS NULL OR category = :category)
        ORDER BY is_pinned DESC, created_at DESC
        """,
    )
    fun observeFiltered(category: String?): Flow<List<ClipboardEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ClipboardEntryEntity): Long

    @Update
    suspend fun update(entity: ClipboardEntryEntity)

    @Delete
    suspend fun delete(entity: ClipboardEntryEntity)

    @Query("DELETE FROM clipboard_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM clipboard_entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM clipboard_entries WHERE id = :id")
    suspend fun getById(id: Long): ClipboardEntryEntity?

    @Query("SELECT * FROM clipboard_entries WHERE content_hash = :hash ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestByHash(hash: String): ClipboardEntryEntity?

    @Query(
        """
        SELECT e.* FROM clipboard_entries AS e
        INNER JOIN clipboard_fts ON e.id = clipboard_fts.rowid
        WHERE clipboard_fts MATCH :matchQuery
        ORDER BY e.is_pinned DESC, e.created_at DESC
        """,
    )
    fun searchMatch(matchQuery: String): Flow<List<ClipboardEntryEntity>>

    @Transaction
    suspend fun trimOldestNonPinned(maxCount: Int) {
        val count = countRows()
        if (count <= maxCount) return
        val toDelete = count - maxCount
        deleteOldestNonPinned(toDelete)
    }

    @Query("SELECT COUNT(*) FROM clipboard_entries")
    suspend fun countRows(): Int

    @Query(
        """
    DELETE FROM clipboard_entries WHERE id IN (
      SELECT id FROM clipboard_entries
      WHERE is_pinned = 0
      ORDER BY created_at ASC
      LIMIT :limit
    )
        """,
    )
    suspend fun deleteOldestNonPinned(limit: Int)
}
