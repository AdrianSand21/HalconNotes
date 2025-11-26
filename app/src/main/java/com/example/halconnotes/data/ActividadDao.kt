package com.example.halconnotes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ActividadDao {
    @Query("SELECT * FROM actividad WHERE id_curso = :cursoId")
    fun obtenerActividadesPorCurso(cursoId: Int): LiveData<List<Actividad>>

    // Necesario para el cálculo matemático manual
    @Query("SELECT * FROM actividad WHERE id_curso = :cursoId")
    suspend fun obtenerListaActividadesSincrona(cursoId: Int): List<Actividad>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarActividad(actividad: Actividad)

    @Delete
    suspend fun eliminarActividad(actividad: Actividad)

    // 👇 ESTE ES EL QUE TE FALTA PROBABLEMENTE
    @Update
    suspend fun actualizarActividad(actividad: Actividad)
}