package com.example.halconnotes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alumno")
data class Alumno(
    // Clave Primaria (PK)
    @PrimaryKey(autoGenerate = true)
    val id_alumno: Int = 0,
    val nombre: String
)