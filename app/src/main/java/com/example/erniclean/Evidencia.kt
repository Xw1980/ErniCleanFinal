package com.example.erniclean

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evidencias")
data class Evidencia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val citaId: Int,
    val uri: String
) 