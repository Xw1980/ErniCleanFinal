package com.example.erniclean

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface EvidenciaDao {
    @Query("SELECT * FROM evidencias WHERE citaId = :citaId")
    suspend fun getEvidenciasPorCita(citaId: Int): List<Evidencia>

    @Insert
    suspend fun insertarEvidencia(evidencia: Evidencia)

    @Delete
    suspend fun eliminarEvidencia(evidencia: Evidencia)
} 