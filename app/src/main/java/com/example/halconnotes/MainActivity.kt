package com.example.halconnotes

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val listaCursos = mutableListOf<Curso>()
    private lateinit var adapter: CursoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvCursos = findViewById<RecyclerView>(R.id.rvCursos)
        rvCursos.layoutManager = LinearLayoutManager(this)

        // Inicializamos el adaptador pasándole la función para el clic largo
        adapter = CursoAdapter(
            listaCursos,
            onCursoClick = { posicion ->
                // Clic Normal: Navegar a NotasActivity
                val intent = android.content.Intent(this, NotasActivity::class.java)
                intent.putExtra("NOMBRE_CURSO", listaCursos[posicion].nombre)
                startActivity(intent)
            },
            onCursoLongClick = { posicion ->
                // Clic Largo: Mostrar opciones (Tu código actual)
                mostrarOpcionesCurso(posicion)
            }
        )
        rvCursos.adapter = adapter

        val fab = findViewById<FloatingActionButton>(R.id.fabAgregar)
        fab.setOnClickListener {
            mostrarDialogoAgregar()
        }
    }

    // --- FUNCIONES DE DIÁLOGOS ---

    // 1. Menú de Opciones (Editar / Eliminar)
    private fun mostrarOpcionesCurso(posicion: Int) {
        val curso = listaCursos[posicion]
        val opciones = arrayOf("Editar Nombre", "Eliminar Materia")

        AlertDialog.Builder(this)
            .setTitle("Opciones para: ${curso.nombre}")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDialogoEditar(posicion) // Opción Editar
                    1 -> mostrarDialogoConfirmarEliminar(posicion) // Opción Eliminar
                }
            }
            .show()
    }

    // 2. Diálogo para AGREGAR (Nuevo)
    private fun mostrarDialogoAgregar() {
        val input = EditText(this)
        input.hint = "Ej. Programación Lógica"

        AlertDialog.Builder(this)
            .setTitle("Nueva Materia")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = input.text.toString()
                if (nombre.isNotEmpty()) {
                    listaCursos.add(Curso(nombre))
                    adapter.notifyItemInserted(listaCursos.size - 1)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 3. Diálogo para EDITAR (Renombrar)
    private fun mostrarDialogoEditar(posicion: Int) {
        val curso = listaCursos[posicion]
        val input = EditText(this)
        input.setText(curso.nombre) // Pre-llenamos con el nombre actual

        AlertDialog.Builder(this)
            .setTitle("Renombrar Materia")
            .setView(input)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevoNombre = input.text.toString()
                if (nuevoNombre.isNotEmpty()) {
                    curso.nombre = nuevoNombre
                    adapter.notifyItemChanged(posicion) // Actualiza solo este renglón visualmente
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 4. Diálogo para ELIMINAR (Confirmación)
    private fun mostrarDialogoConfirmarEliminar(posicion: Int) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar curso?")
            .setMessage("Se borrará '${listaCursos[posicion].nombre}' y todas sus notas.")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                listaCursos.removeAt(posicion)
                adapter.notifyItemRemoved(posicion) // Borra visualmente con animación
                adapter.notifyItemRangeChanged(posicion, listaCursos.size) // Ajusta los índices internos
            }
            .setNegativeButton("No", null)
            .show()
    }
}