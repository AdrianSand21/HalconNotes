package com.example.halconnotes.control

import androidx.lifecycle.ViewModel
import com.example.halconnotes.data.CursoDao

class PromedioViewModel(private val dao: CursoDao) : ViewModel() {

    //Promedio total del alumno
    fun promedio(idAlumno: Int) = dao.obtenerPromedioGeneral(idAlumno)

    // Lista de cursos del alumno
    fun cursos(idAlumno: Int) = dao.obtenerCursosDeAlumno(idAlumno)
}
