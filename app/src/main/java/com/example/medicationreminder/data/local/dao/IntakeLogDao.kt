package com.example.medicationreminder.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medicationreminder.data.local.entity.IntakeLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeLogDao {

    @Query("SELECT * FROM intake_logs WHERE scheduledDate = :date ORDER BY scheduledTime ASC")
    fun observeForDate(date: String): Flow<List<IntakeLogEntity>>

    @Query("SELECT * FROM intake_logs WHERE medicationId = :medicationId AND scheduledDate = :date")
    suspend fun get(medicationId: Long, date: String): IntakeLogEntity?

    @Query(
        """
        SELECT * FROM intake_logs
        WHERE scheduledDate BETWEEN :from AND :to
        ORDER BY scheduledDate DESC, scheduledTime ASC
        """
    )
    fun observeBetween(from: String, to: String): Flow<List<IntakeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: IntakeLogEntity)

    @Update
    suspend fun update(log: IntakeLogEntity)
}
