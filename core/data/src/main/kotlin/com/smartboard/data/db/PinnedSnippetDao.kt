package com.smartboard.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.smartboard.data.db.entities.PinnedSnippetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedSnippetDao {

    @Query("SELECT * FROM pinned_snippets ORDER BY sort_order ASC, id ASC")
    fun observeAll(): Flow<List<PinnedSnippetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PinnedSnippetEntity): Long

    @Update
    suspend fun update(entity: PinnedSnippetEntity)

    @Delete
    suspend fun delete(entity: PinnedSnippetEntity)

    @Query("DELETE FROM pinned_snippets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM pinned_snippets")
    suspend fun count(): Int

    @Transaction
    suspend fun replaceAll(entities: List<PinnedSnippetEntity>) {
        deleteAll()
        entities.forEach { insert(it) }
    }

    @Query("SELECT * FROM pinned_snippets WHERE id = :id")
    suspend fun getById(id: Long): PinnedSnippetEntity?

    @Query("DELETE FROM pinned_snippets")
    suspend fun deleteAll()
}
