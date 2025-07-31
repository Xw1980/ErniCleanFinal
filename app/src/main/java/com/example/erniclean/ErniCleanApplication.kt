package com.example.erniclean

import android.app.Application
import androidx.room.Room
import androidx.appcompat.app.AppCompatDelegate

class ErniCleanApplication : Application() {
    companion object {
        lateinit var database: ErniCleanDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        database = Room.databaseBuilder(
            applicationContext,
            ErniCleanDatabase::class.java,
            "erniclean-db"
        ).build()
    }
} 