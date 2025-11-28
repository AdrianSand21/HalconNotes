package com.example.halconnotes.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.halconnotes.data.CursoDao

class PromedioViewModelFactory(
    private val dao: CursoDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromedioViewModel::class.java)) {
            return PromedioViewModel(dao) as T
        }
        throw IllegalArgumentException("ViewModel no encontrado")
    }
}

