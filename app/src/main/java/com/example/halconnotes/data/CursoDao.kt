package com.example.halconnotes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CursoDao {

    @Query("SELECT * FROM curso ORDER BY nombre ASC")
    fun obtenerTodosLosCursos(): LiveData<List<Curso>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCurso(curso: Curso)

    @Delete
    suspend fun eliminarCurso(curso: Curso)

    @Update
    suspend fun actualizarCurso(curso: Curso)

    // 👇 ESTE ES EL QUE TE FALTABA PARA EL MÓDULO 3
    @Query("UPDATE curso SET promedioActual = :nuevoPromedio WHERE id_curso = :idCurso")
    suspend fun actualizarPromedio(idCurso: Int, nuevoPromedio: Float)
}