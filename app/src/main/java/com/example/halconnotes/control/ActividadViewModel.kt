package com.example.halconnotes.control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.halconnotes.data.BD
import com.example.halconnotes.data.ActividadDao
import com.example.halconnotes.data.Actividad // Importamos la Entidad desde 'datos'
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActividadViewModel(application: Application) : AndroidViewModel(application) {

    private val actividadDao: ActividadDao

    init {
        // Inicializa el DAO usando el Singleton de la BD
        val database = BD.getDatabase(application)
        actividadDao = database.actividadDao()
    }

    // Obtener actividades de UN solo curso (Módulo 2)
    fun obtenerActividadesDeCurso(cursoId: Int): LiveData<List<Actividad>> {
        return actividadDao.obtenerActividadesPorCurso(cursoId)
    }

    // Insertar nueva actividad (Módulo 2)
    fun insertarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.insertarActividad(actividad)
        }
    }

    // Eliminar actividad
    fun eliminarActividad(actividad: Actividad) {
        viewModelScope.launch(Dispatchers.IO) {
            actividadDao.eliminarActividad(actividad)
        }
    }
}