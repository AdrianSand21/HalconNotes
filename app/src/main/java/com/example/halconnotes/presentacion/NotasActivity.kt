package com.example.halconnotes.presentacion

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.control.ActividadViewModel
import com.example.halconnotes.data.Actividad
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotasActivity : AppCompatActivity() {

    private val actividadViewModel: ActividadViewModel by viewModels()
    private lateinit var adapter: ActividadAdapter
    private var idCursoActual: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notas)

        // 1. Recibir datos del MainActivity
        val nombreCurso = intent.getStringExtra("NOMBRE_CURSO") ?: "Curso"
        idCursoActual = intent.getIntExtra("ID_CURSO", -1)

        if (idCursoActual == -1) {
            Toast.makeText(this, "Error al cargar el curso", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Referencias a la UI
        val tvTitulo = findViewById<TextView>(R.id.tvTituloMateria)
        val tvPesoTotal = findViewById<TextView>(R.id.tvPesoTotal)          // Barra resumen
        val tvCalificacionTotal = findViewById<TextView>(R.id.tvCalificacionTotal) // Barra resumen
        val rvNotas = findViewById<RecyclerView>(R.id.rvNotas)
        val fab = findViewById<FloatingActionButton>(R.id.fabAgregarNota)

        tvTitulo.text = nombreCurso

        // 3. Configurar RecyclerView y Adaptador
        rvNotas.layoutManager = LinearLayoutManager(this)

        adapter = ActividadAdapter(
            onItemClick = { actividad ->
                mostrarDialogoEditarActividad(actividad) // Clic corto: Editar
            },
            onLongClick = { actividad ->
                mostrarDialogoEliminar(actividad) // Clic largo: Eliminar
            }
        )
        rvNotas.adapter = adapter

        // 4. Observar la Base de Datos (Lógica Módulo 2 y 3)
        actividadViewModel.obtenerActividadesDeCurso(idCursoActual).observe(this) { actividades ->

            // Actualizar lista visual
            adapter.actualizarLista(actividades)

            // --- CÁLCULO DEL PROMEDIO (MÓDULO 3) ---
            var sumaPesos = 0f
            var sumaPuntos = 0f

            for (act in actividades) {
                sumaPesos += act.peso
                // (Nota / 10) * Peso. Ajusta '10f' si tu escala es 100
                sumaPuntos += (act.calificacion / 10f) * act.peso
            }

            // Actualizar textos de resumen
            val promedioFormateado = String.format("%.2f", sumaPuntos)

            tvPesoTotal.text = "Progreso: $sumaPesos / 100 %"
            tvCalificacionTotal.text = "Acumulado: $promedioFormateado"

            // Cambio de color al completar
            if (sumaPesos >= 100f) {
                tvPesoTotal.setTextColor(android.graphics.Color.parseColor("#388E3C")) // Verde
                tvPesoTotal.text = "¡Materia Completa! (100%)"

                tvCalificacionTotal.text = "FINAL: $promedioFormateado"
                tvCalificacionTotal.textSize = 22f
            } else {
                tvPesoTotal.setTextColor(android.graphics.Color.BLACK)
                tvCalificacionTotal.textSize = 18f
            }
        }

        // 5. Botón Agregar (¡Ahora sí funcionará!)
        fab.setOnClickListener {
            mostrarDialogoAgregarNota()
        }
    }

    // --- FUNCIONES DE DIÁLOGOS ---

    private fun mostrarDialogoAgregarNota() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre (Ej. Examen 1)"
        layout.addView(inputNombre)

        val inputPeso = EditText(this)
        inputPeso.hint = "Peso % (Ej. 30)"
        inputPeso.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputPeso)

        val inputNota = EditText(this)
        inputNota.hint = "Calificación (Ej. 9.5)"
        inputNota.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputNota)

        AlertDialog.Builder(this)
            .setTitle("Nueva Actividad")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString()
                val peso = inputPeso.text.toString().toFloatOrNull()
                val nota = inputNota.text.toString().toFloatOrNull()

                if (nombre.isNotEmpty() && peso != null && nota != null) {
                    // Guardar en BD
                    val nuevaActividad = Actividad(
                        id_curso = idCursoActual, // Vinculamos al curso actual
                        nombre = nombre,
                        peso = peso,
                        calificacion = nota
                    )
                    actividadViewModel.insertarActividad(nuevaActividad)
                } else {
                    Toast.makeText(this, "Datos incompletos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditarActividad(actividad: Actividad) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputNombre = EditText(this)
        inputNombre.setText(actividad.nombre)
        layout.addView(inputNombre)

        val inputPeso = EditText(this)
        inputPeso.setText(actividad.peso.toString())
        inputPeso.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputPeso)

        val inputNota = EditText(this)
        inputNota.setText(actividad.calificacion.toString())
        inputNota.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputNota)

        AlertDialog.Builder(this)
            .setTitle("Editar Actividad")
            .setView(layout)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevoNombre = inputNombre.text.toString()
                val nuevoPeso = inputPeso.text.toString().toFloatOrNull()
                val nuevaNota = inputNota.text.toString().toFloatOrNull()

                if (nuevoNombre.isNotEmpty() && nuevoPeso != null && nuevaNota != null) {
                    val actividadEditada = actividad.copy(
                        nombre = nuevoNombre,
                        peso = nuevoPeso,
                        calificacion = nuevaNota
                    )
                    actividadViewModel.actualizarActividad(actividadEditada)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEliminar(actividad: Actividad) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar")
            .setMessage("¿Borrar '${actividad.nombre}'?")
            .setPositiveButton("Sí") { _, _ ->
                actividadViewModel.eliminarActividad(actividad)
            }
            .setNegativeButton("No", null)
            .show()
    }
}