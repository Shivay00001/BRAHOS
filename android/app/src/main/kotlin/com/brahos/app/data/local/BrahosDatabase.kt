package com.brahos.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ConsultationEntity::class], version = 1, exportSchema = false)
@TypeConverters(RiskLevelConverter::class)
abstract class BrahosDatabase : RoomDatabase() {
    abstract fun consultationDao(): ConsultationDao
}
