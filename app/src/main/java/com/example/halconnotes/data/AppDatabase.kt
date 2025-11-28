package com.example.halconnotes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Curso::class,Actividad::class, Alumno::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cursoDao(): CursoDao
    abstract fun actividadDao(): ActividadDao
    abstract fun alumnoDao(): AlumnoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "halconnotes_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
