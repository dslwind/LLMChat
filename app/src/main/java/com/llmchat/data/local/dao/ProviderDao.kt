package com.llmchat.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.llmchat.data.local.entity.Provider
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY isDefault DESC, name ASC")
    fun observeAll(): Flow<List<Provider>>

    @Query("SELECT * FROM providers WHERE id = :id")
    fun observeById(id: String): Flow<Provider?>

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getById(id: String): Provider?

    @Query("SELECT * FROM providers WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): Provider?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provider: Provider)

    @Update
    suspend fun update(provider: Provider)

    @Delete
    suspend fun delete(provider: Provider)

    @Query("UPDATE providers SET isDefault = 0")
    suspend fun clearDefault()
}
