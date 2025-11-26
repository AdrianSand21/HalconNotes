package com.example.halconnotes.presentacion

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.activity.viewModels // Necesario para instanciar el ViewModel
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.control.CursoViewModel
import com.example.halconnotes.data.Curso
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    // 1. Instanciamos el ViewModel (El intermediario con la BD)
    private val cursoViewModel: CursoViewModel by viewModels()

    private lateinit var adapter: CursoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvCursos = findViewById<RecyclerView>(R.id.rvCursos)
        rvCursos.layoutManager = LinearLayoutManager(this)

        // 2. Configuramos el adapter (ya no le pasamos una lista vacía)
        adapter = CursoAdapter(
            onCursoClick = { posicion ->
                val curso = adapter.obtenerCurso(posicion)
                val intent = Intent(this, NotasActivity::class.java)
                intent.putExtra("NOMBRE_CURSO", curso.nombre)
                // También deberías pasar el ID para cargar las notas correctas
                intent.putExtra("ID_CURSO", curso.id_curso)
                startActivity(intent)
            },
            onCursoLongClick = { posicion ->
                mostrarOpcionesCurso(posicion)
            }
        )
        rvCursos.adapter = adapter

        // 3. OBSERVAMOS la Base de Datos
        // Cada vez que la BD cambie, este código se ejecuta automáticamente
        cursoViewModel.todosLosCursos.observe(this) { cursosCargados ->
            // Le pasamos los datos reales al adaptador
            adapter.actualizarLista(cursosCargados)
        }

        val fab = findViewById<FloatingActionButton>(R.id.fabAgregar)
        fab.setOnClickListener {
            mostrarDialogoAgregar()
        }
    }

    // --- FUNCIONES DE DIÁLOGOS CONECTADAS A LA BD ---

    private fun mostrarDialogoAgregar() {
        val input = EditText(this)
        input.hint = "Ej. Programación Lógica"

        AlertDialog.Builder(this)
            .setTitle("Nueva Materia")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = input.text.toString()
                if (nombre.isNotEmpty()) {
                    // CAMBIO: En lugar de añadir a una lista local, le decimos al ViewModel
                    val nuevoCurso = Curso(id_alumno = 1, nombre = nombre)
                    cursoViewModel.insertarCurso(nuevoCurso)
                    // No necesitamos 'notifyInserted', el Observer de arriba lo hará solo
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditar(posicion: Int) {
        val cursoActual = adapter.obtenerCurso(posicion)
        val input = EditText(this)
        input.setText(cursoActual.nombre)

        AlertDialog.Builder(this)
            .setTitle("Renombrar Materia")
            .setView(input)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevoNombre = input.text.toString()
                if (nuevoNombre.isNotEmpty()) {
                    // CAMBIO: Creamos copia y mandamos actualizar a BD
                    val cursoEditado = cursoActual.copy(nombre = nuevoNombre)
                    cursoViewModel.actualizarCurso(cursoEditado)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoConfirmarEliminar(posicion: Int) {
        val curso = adapter.obtenerCurso(posicion)
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar curso?")
            .setMessage("Se borrará '${curso.nombre}' y todas sus notas.")
            .setPositiveButton("Sí, eliminar") { _, _ ->
                // CAMBIO: Mandamos eliminar a la BD
                cursoViewModel.eliminarCurso(curso)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun mostrarOpcionesCurso(posicion: Int) {
        val curso = adapter.obtenerCurso(posicion)
        val opciones = arrayOf("Editar Nombre", "Eliminar Materia")
        AlertDialog.Builder(this)
            .setTitle("Opciones para: ${curso.nombre}")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarDialogoEditar(posicion)
                    1 -> mostrarDialogoConfirmarEliminar(posicion)
                }
            }
            .show()
    }
}