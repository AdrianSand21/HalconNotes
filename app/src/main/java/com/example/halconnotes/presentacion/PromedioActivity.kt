package com.example.halconnotes.presentacion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.control.PromedioViewModel
import com.example.halconnotes.control.PromedioViewModelFactory
import com.example.halconnotes.control.EscalaManager
import com.example.halconnotes.data.BD

class PromedioActivity : AppCompatActivity() {

    private lateinit var viewModel: PromedioViewModel
    private lateinit var adapter: MateriasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.promedio)

        // Habilitar modo pantalla completa (Edge-to-Edge)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = findViewById<android.view.View>(R.id.root_promedio)
        val toolbar = findViewById<android.view.View>(R.id.toolbar)

        // Ajustar el padding del Toolbar y el padding inferior del root
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            
            // 1. Arriba: Padding solo al Toolbar
            toolbar.setPadding(0, systemBars.top, 0, 0)
            
            // 2. Abajo: Padding al contenedor raíz para proteger el contenido inferior
            view.setPadding(0, 0, 0, systemBars.bottom)
            
            insets
        }

        val txtPromedio = findViewById<TextView>(R.id.txtPromedioGeneral)
        val recycler = findViewById<RecyclerView>(R.id.recyclerMaterias)
        recycler.layoutManager = LinearLayoutManager(this)
        
        // Configuración del Toolbar y Navegación
        (toolbar as com.google.android.material.appbar.MaterialToolbar).setNavigationOnClickListener {
            finish()
        }

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
            // Aplicar conversión de escala visual al promedio general
            val currentScale = EscalaManager.getCurrentScale(this)
            val promedioVisual = EscalaManager.convert(promedio ?: 0.0, currentScale)
            txtPromedio.text = promedioVisual
        }
    }
}