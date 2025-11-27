package com.example.halconnotes.control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.halconnotes.data.BD
import com.example.halconnotes.data.ActividadDao
import com.example.halconnotes.data.CursoDao // <--- NECESARIO PARA ACTUALIZAR EL PROMEDIO DEL CURSO
import com.example.halconnotes.data.Actividad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActividadViewModel(application: Application) : AndroidViewModel(application) {

    private val actividadDao: ActividadDao
    private val cursoDao: CursoDao // <--- Referencia a la tabla de Cursos

    init {
        val database = BD.getDatabase(application)
        actividadDao = database.actividadDao()
        cursoDao = database.cursoDao() // <--- Inicializamos el DAO de Cursos
    }

    // Obtener actividades para la lista (Módulo 2)
    fun obtenerActividadesDeCurso(cursoId: Int): LiveData<List<Actividad>> {
        return actividadDao.obtenerActividadesPorCurso(cursoId)
    }

    // 1. INSERTAR (y recalcular promedio)
    fun insertarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.insertarActividad(actividad)
            // Cada vez que cambiamos algo, recalculamos el promedio general
            recalcularYActualizarPromedio(actividad.id_curso)
        }
    }

    // 2. ACTUALIZAR (El que te faltaba)
    fun actualizarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.actualizarActividad(actividad)
            recalcularYActualizarPromedio(actividad.id_curso)
        }
    }

    // 3. ELIMINAR (y recalcular promedio)
    fun eliminarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.eliminarActividad(actividad)
            recalcularYActualizarPromedio(actividad.id_curso)
        }
    }

    // --- LÓGICA DEL MÓDULO 3 (Motor matemático) ---
    private suspend fun recalcularYActualizarPromedio(cursoId: Int) {
        // Obtenemos la lista "cruda" de actividades para sumar
        val lista = actividadDao.obtenerListaActividadesSincrona(cursoId)

        var sumaPuntos = 0f

        for (act in lista) {
            // Fórmula: (Calificación / 10) * Peso
            // Ejemplo: (9.0 / 10) * 20 = 1.8 puntos
            val puntos = (act.calificacion / 10f) * act.peso
            sumaPuntos += puntos
        }

        // Guardamos el nuevo promedio en la tabla Curso para que se vea en el inicio
        cursoDao.actualizarPromedio(cursoId, sumaPuntos)
    }
}