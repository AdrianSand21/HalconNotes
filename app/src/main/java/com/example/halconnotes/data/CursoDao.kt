package com.example.halconnotes.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CursoDao {

    // Obtener todos los cursos para la lista principal
    @Query("SELECT * FROM curso ORDER BY nombre ASC")
    fun obtenerTodosLosCursos(): LiveData<List<Curso>>

    // Insertar nuevo curso
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCurso(curso: Curso)

    // Eliminar curso
    @Delete
    suspend fun eliminarCurso(curso: Curso)

    // Renombrar curso (Actualizar todo el objeto)
    @Update
    suspend fun actualizarCurso(curso: Curso)

    // 👇 ESTE ES EL QUE FALTABA PARA EL MÓDULO 3 👇
    // Actualiza SOLO el promedio de un curso específico
    @Query("UPDATE curso SET promedioActual = :nuevoPromedio WHERE id_curso = :idCurso")
    suspend fun actualizarPromedio(idCurso: Int, nuevoPromedio: Float)
}