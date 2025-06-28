package com.example.erniclean

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen

class ErniCleanApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
    }
} 