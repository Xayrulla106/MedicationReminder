package com.example.medicationreminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.medicationreminder.data.local.dao.IntakeLogDao
import com.example.medicationreminder.data.local.dao.MedicationDao
import com.example.medicationreminder.data.local.entity.IntakeLogEntity
import com.example.medicationreminder.data.local.entity.MedicationEntity

@Database(
    entities = [MedicationEntity::class, IntakeLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : androidx.room.RoomDatabase() {

    abstract fun medicationDao(): MedicationDao
    abstract fun intakeLogDao(): IntakeLogDao

    companion object {
        const val DATABASE_NAME = "medication_reminder.db"
    }
}
