package com.example.halconnotes.presentacion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.data.AppDatabase
import com.example.halconnotes.control.PromedioViewModel
import com.example.halconnotes.control.PromedioViewModelFactory
import com.example.halconnotes.data.BD

class PromedioActivity : AppCompatActivity() {

    private lateinit var viewModel: PromedioViewModel
    private lateinit var adapter: MateriasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.promedio)

        val txtPromedio = findViewById<TextView>(R.id.txtPromedioGeneral)
        val recycler = findViewById<RecyclerView>(R.id.recyclerMaterias)
        recycler.layoutManager = LinearLayoutManager(this)

        // Crear ViewModel
        val dao = BD.getDatabase(this).cursoDao()
        viewModel = ViewModelProvider(
            this,
            PromedioViewModelFactory(dao)
        )[PromedioViewModel::class.java]

        val idAlumno = 1

        adapter = MateriasAdapter()
        recycler.adapter = adapter

        // la lista de cursos
        viewModel.cursos(idAlumno).observe(this) { lista ->
            adapter.actualizar(lista)
        }

        // promedio general
        viewModel.promedio(idAlumno).observe(this) { promedio ->
            txtPromedio.text = "%.2f".format(promedio ?: 0.0)
        }
    }
}






