package com.example.halconnotes.data // Ajusta este paquete si tu app se llama diferente

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Alumno::class, Curso::class, Actividad::class],
    version = 2,
    exportSchema = false
)
abstract class BD : RoomDatabase() {

    // DAOs (Data Access Objects)
    abstract fun alumnoDao(): AlumnoDao
    abstract fun cursoDao(): CursoDao
    abstract fun actividadDao(): ActividadDao

    // Patrón Singleton (permite una única instancia de la BD)
    companion object {
        @Volatile
        private var INSTANCE: BD? = null

        fun getDatabase(context: Context): BD {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BD::class.java,
                    "proyectotap_db" // Nombre del archivo físico de SQLite
                )
                    // Usamos esto para recrear la BD si la estructura cambia (necesario al subir la versión a 2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}