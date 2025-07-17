package com.example.erniclean

import androidx.room.Room
import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class ErniCleanApplication : Application() {
    companion object {
        lateinit var database: ErniCleanDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        database = Room.databaseBuilder(
            applicationContext,
            ErniCleanDatabase::class.java,
            "erniclean-db"
        ).build()
    }
} 