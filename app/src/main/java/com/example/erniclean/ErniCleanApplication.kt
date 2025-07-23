package com.example.erniclean

import android.app.Application
import androidx.room.Room

class ErniCleanApplication : Application() {
    companion object {
        lateinit var database: ErniCleanDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            ErniCleanDatabase::class.java,
            "erniclean-db"
        ).build()
    }
} 