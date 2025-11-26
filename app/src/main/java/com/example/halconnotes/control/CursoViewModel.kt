package com.example.halconnotes.control

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.halconnotes.data.BD
import com.example.halconnotes.data.CursoDao
import com.example.halconnotes.data.Curso
import com.example.halconnotes.data.AlumnoDao
import com.example.halconnotes.data.Alumno
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CursoViewModel(application: Application) : AndroidViewModel(application) {

    private val database = BD.getDatabase(application)
    private val cursoDao = database.cursoDao()
    private val alumnoDao = database.alumnoDao()

    val todosLosCursos: LiveData<List<Curso>> = cursoDao.obtenerTodosLosCursos()

    init {
        inicializarAlumnoDefault()
    }

    private fun inicializarAlumnoDefault() {
        viewModelScope.launch(Dispatchers.IO) {
            val alumno = Alumno(id_alumno = 1, nombre = "Estudiante Principal")
            alumnoDao.insertarAlumno(alumno)
        }
    }

    fun insertarCurso(curso: Curso) {
        viewModelScope.launch(Dispatchers.IO) {
            val cursoConAlumno = curso.copy(id_alumno = 1)
            cursoDao.insertarCurso(cursoConAlumno)
        }
    }

    fun eliminarCurso(curso: Curso) {
        viewModelScope.launch(Dispatchers.IO) {
            cursoDao.eliminarCurso(curso)
        }
    }

    fun actualizarCurso(curso: Curso) {
        viewModelScope.launch(Dispatchers.IO) {
            cursoDao.actualizarCurso(curso)
        }
    }
}