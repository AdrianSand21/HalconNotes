package com.example.halconnotes.control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.halconnotes.data.BD
import com.example.halconnotes.data.ActividadDao
import com.example.halconnotes.data.CursoDao
import com.example.halconnotes.data.Actividad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActividadViewModel(application: Application) : AndroidViewModel(application) {

    private val actividadDao: ActividadDao
    private val cursoDao: CursoDao

    init {
        val database = BD.getDatabase(application)
        actividadDao = database.actividadDao()
        cursoDao = database.cursoDao()
    }

    fun obtenerActividadesDeCurso(cursoId: Int): LiveData<List<Actividad>> {
        return actividadDao.obtenerActividadesPorCurso(cursoId)
    }

    // 1. Insertar
    fun insertarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.insertarActividad(actividad)
            recalcularYActualizarPromedio(actividad.id_curso)
        }
    }

    // 2. Actualizar (Aquí te daba el error)
    fun actualizarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.actualizarActividad(actividad)
            recalcularYActualizarPromedio(actividad.id_curso)
        }
    }

    // 3. Eliminar
    fun eliminarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.eliminarActividad(actividad)
            recalcularYActualizarPromedio(actividad.id_curso)
        }
    }

    // --- LÓGICA DEL MÓDULO 3 (Cálculo de Promedio) ---
    // Si te faltaba esta función privada, por eso marcaba error arriba.
    private suspend fun recalcularYActualizarPromedio(cursoId: Int) {
        // Obtenemos la lista cruda para hacer matemáticas
        val lista = actividadDao.obtenerListaActividadesSincrona(cursoId)

        var sumaPuntos = 0f

        for (act in lista) {
            // Fórmula: (Calificación / 10) * Peso
            // Ajusta el '10f' si tu escala es sobre 100
            val puntos = (act.calificacion / 10f) * act.peso
            sumaPuntos += puntos
        }

        // Actualizamos la tabla Curso con el nuevo promedio total
        cursoDao.actualizarPromedio(cursoId, sumaPuntos)
    }
}