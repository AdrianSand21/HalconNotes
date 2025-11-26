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
import com.example.halconnotes.data.Actividad // Asegúrate de que sea 'data' o 'datos' según tu carpeta
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotasActivity : AppCompatActivity() {

    private val actividadViewModel: ActividadViewModel by viewModels()
    private lateinit var adapter: ActividadAdapter
    private var idCursoActual: Int = -1 // Aquí guardaremos el ID del curso

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notas)

        // 1. Recibir datos del MainActivity (Nombre e ID del curso)
        val nombreCurso = intent.getStringExtra("NOMBRE_CURSO") ?: "Curso"
        idCursoActual = intent.getIntExtra("ID_CURSO", -1)

        // Validación de seguridad: Si no hay ID, cerramos la pantalla
        if (idCursoActual == -1) {
            Toast.makeText(this, "Error al cargar el curso", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2. Referencias a la Interfaz de Usuario (UI)
        val tvTitulo = findViewById<TextView>(R.id.tvTituloMateria)
        val tvPesoTotal = findViewById<TextView>(R.id.tvPesoTotal)          // <-- NUEVO
        val tvCalificacionTotal = findViewById<TextView>(R.id.tvCalificacionTotal) // <-- NUEVO
        val rvNotas = findViewById<RecyclerView>(R.id.rvNotas)
        val fab = findViewById<FloatingActionButton>(R.id.fabAgregarNota)

        // Configurar Título
        tvTitulo.text = nombreCurso

        // 3. Configurar el RecyclerView (Lista)
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

        // 4. Observar la Base de Datos y CALCULAR TOTALES
        // Este bloque se ejecuta cada vez que agregas, borras o editas una nota
        actividadViewModel.obtenerActividadesDeCurso(idCursoActual).observe(this) { actividades ->

            // A. Actualizar la lista visual
            adapter.actualizarLista(actividades)

            // B. Cálculo matemático del resumen
            var sumaPesos = 0f
            var sumaPuntos = 0f

            for (act in actividades) {
                sumaPesos += act.peso
                // Fórmula: (Nota / 10) * Peso. Ajusta '10f' si tu escala es 100
                sumaPuntos += (act.calificacion / 10f) * act.peso
            }

            // C. Actualizar los textos de resumen
            // Formato a 2 decimales para que se vea limpio
            val promedioFormateado = String.format("%.2f", sumaPuntos)

            tvPesoTotal.text = "Progreso: $sumaPesos / 100 %"
            tvCalificacionTotal.text = "Acumulado: $promedioFormateado"

            // D. Lógica de Colores y Validación (Feedback visual)
            if (sumaPesos >= 100f) {
                // Si completó el 100%, ponemos todo en VERDE
                tvPesoTotal.setTextColor(android.graphics.Color.parseColor("#388E3C"))
                tvPesoTotal.text = "Materia al 100%"

                tvCalificacionTotal.text = "FINAL: $promedioFormateado"
                tvCalificacionTotal.textSize = 22f // Hacemos el texto más grande
                tvCalificacionTotal.setTextColor(android.graphics.Color.parseColor("#388E3C"))
            } else {
                // Si aún falta, colores normales (Negro y Morado)
                tvPesoTotal.setTextColor(android.graphics.Color.BLACK)
                tvCalificacionTotal.textSize = 18f
                tvCalificacionTotal.setTextColor(android.graphics.Color.parseColor("#6200EE"))
            }
        }

        // 5. Botón Agregar
        fab.setOnClickListener {
            // Opcional: Puedes bloquear el botón si ya llegó al 100% revisando 'tvPesoTotal.text'
            mostrarDialogoAgregarNota()
        }
    }

    // --- DIÁLOGOS ---

    // 1. AGREGAR NUEVA ACTIVIDAD
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
                    // Crear y guardar la actividad vinculada al ID del curso
                    val nuevaActividad = Actividad(
                        id_curso = idCursoActual,
                        nombre = nombre,
                        peso = peso,
                        calificacion = nota
                    )
                    actividadViewModel.insertarActividad(nuevaActividad)
                } else {
                    Toast.makeText(this, "Por favor llena todos los datos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 2. EDITAR ACTIVIDAD EXISTENTE
    private fun mostrarDialogoEditarActividad(actividad: Actividad) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        // Pre-llenamos los campos con los datos actuales
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
                    // Creamos una COPIA con los datos nuevos (id se mantiene igual)
                    val actividadEditada = actividad.copy(
                        nombre = nuevoNombre,
                        peso = nuevoPeso,
                        calificacion = nuevaNota
                    )
                    // Llamamos al ViewModel para actualizar
                    actividadViewModel.actualizarActividad(actividadEditada)
                } else {
                    Toast.makeText(this, "Datos inválidos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 3. ELIMINAR ACTIVIDAD
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