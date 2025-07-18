package com.example.erniclean

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Evidencia::class], version = 1)
abstract class ErniCleanDatabase : RoomDatabase() {
    abstract fun evidenciaDao(): EvidenciaDao
} 