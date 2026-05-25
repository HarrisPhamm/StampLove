package com.example.data.db

import androidx.room.*
import com.example.data.model.Stamp
import kotlinx.coroutines.flow.Flow

@Dao
interface StampDao {
    @Query("SELECT * FROM stamps ORDER BY dateCreated DESC")
    fun getAllStampsFlow(): Flow<List<Stamp>>

    @Query("SELECT * FROM stamps WHERE category = :categoryName ORDER BY dateCreated DESC")
    fun getStampsByCategoryFlow(categoryName: String): Flow<List<Stamp>>

    @Query("SELECT * FROM stamps WHERE name LIKE :searchQuery OR note LIKE :searchQuery ORDER BY dateCreated DESC")
    fun searchStampsFlow(searchQuery: String): Flow<List<Stamp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStamp(stamp: Stamp)

    @Update
    suspend fun updateStamp(stamp: Stamp)

    @Delete
    suspend fun deleteStamp(stamp: Stamp)

    @Query("SELECT * FROM stamps WHERE id = :stampId LIMIT 1")
    suspend fun getStampById(stampId: Int): Stamp?
}
