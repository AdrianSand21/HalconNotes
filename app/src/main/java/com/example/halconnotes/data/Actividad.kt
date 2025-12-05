package com.example.halconnotes.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "actividad",
    foreignKeys = [ForeignKey(
        entity = Curso::class,
        parentColumns = ["id_curso"],
        childColumns = ["id_curso"],
        onDelete = ForeignKey.CASCADE
    )]
)

data class Actividad(
    @PrimaryKey(autoGenerate = true)
    val id_actividad: Int = 0,
    val id_curso: Int,
    val nombre: String,
    val peso: Float,
    val calificacion: Float

)
