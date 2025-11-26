package com.example.halconnotes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface AlumnoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // <--- IMPORTANTE: IGNORE
    suspend fun insertarAlumno(alumno: Alumno)
}