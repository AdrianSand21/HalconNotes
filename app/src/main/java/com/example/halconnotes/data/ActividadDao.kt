package com.example.halconnotes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ActividadDao {

    // Obtener lista para mostrar en pantalla (LiveData)
    @Query("SELECT * FROM actividad WHERE id_curso = :cursoId")
    fun obtenerActividadesPorCurso(cursoId: Int): LiveData<List<Actividad>>

    // 👇 IMPORTANTE: Obtener lista CRUDA para sumar en el ViewModel (Suspend)
    @Query("SELECT * FROM actividad WHERE id_curso = :cursoId")
    suspend fun obtenerListaActividadesSincrona(cursoId: Int): List<Actividad>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarActividad(actividad: Actividad)

    @Delete
    suspend fun eliminarActividad(actividad: Actividad)

    // 👇 ESTE ES EL QUE TE FALTABA
    @Update
    suspend fun actualizarActividad(actividad: Actividad)
}