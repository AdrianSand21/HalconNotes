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
    fun insertarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.insertarActividad(actividad)
            recalcularYActualizarPromedio(actividad.id_curso)
        }
    }
    fun actualizarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.actualizarActividad(actividad)
            recalcularYActualizarPromedio(actividad.id_curso)
        }
    }
    fun eliminarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.eliminarActividad(actividad)
            recalcularYActualizarPromedio(actividad.id_curso)
        }
    }

    private suspend fun recalcularYActualizarPromedio(cursoId: Int) {
        // Obtenemos la lista "cruda" de actividades para sumar
        val lista = actividadDao.obtenerListaActividadesSincrona(cursoId)

        var sumaPuntos = 0f

        for (act in lista) {
            val puntos = (act.calificacion / 100f) * act.peso
            sumaPuntos += puntos
        }
        cursoDao.actualizarPromedio(cursoId, sumaPuntos)
    }
}