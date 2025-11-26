package com.example.halconnotes.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// 👇 IMPORTANTE: tableName debe ser "curso" para que el DAO lo encuentre
@Entity(
    tableName = "curso",
    foreignKeys = [ForeignKey(
        entity = Alumno::class,
        parentColumns = ["id_alumno"],
        childColumns = ["id_alumno"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Curso(
    @PrimaryKey(autoGenerate = true)
    val id_curso: Int = 0,

    val id_alumno: Int,
    val nombre: String, // 👇 IMPORTANTE: Debe llamarse "nombre" para que el ORDER BY funcione
    val promedioActual: Float = 0f
)