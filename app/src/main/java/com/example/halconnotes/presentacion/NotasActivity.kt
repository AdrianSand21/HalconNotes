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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.control.ActividadViewModel
import com.example.halconnotes.control.ScaleManager
import com.example.halconnotes.data.Actividad
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotasActivity : AppCompatActivity() {

    private val actividadViewModel: ActividadViewModel by viewModels()
    private lateinit var adapter: ActividadAdapter
    private var idCursoActual: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notas)

        val nombreCurso = intent.getStringExtra("NOMBRE_CURSO") ?: "Curso"
        idCursoActual = intent.getIntExtra("ID_CURSO", -1)

        if (idCursoActual == -1) {
            Toast.makeText(this, "Error al cargar el curso", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // CONFIGURACIÓN DEL TOOLBAR Y NAVEGACIÓN
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.title = nombreCurso // Asigna el nombre de la materia al encabezado
        toolbar.setNavigationOnClickListener {
            finish() // Cierra la actividad
        }

        // Eliminamos referencia al antiguo título
        // val tvTitulo = findViewById<TextView>(R.id.tvTituloMateria)
        val tvPesoTotal = findViewById<TextView>(R.id.tvPesoTotal)
        val tvCalificacionTotal = findViewById<TextView>(R.id.tvCalificacionTotal)
        val rvNotas = findViewById<RecyclerView>(R.id.rvNotas)
        val fab = findViewById<FloatingActionButton>(R.id.fabAgregarNota)

        // tvTitulo.text = nombreCurso // Ya lo hace el toolbar

        rvNotas.layoutManager = LinearLayoutManager(this)

        adapter = ActividadAdapter(
            onItemClick = { actividad ->
                mostrarDialogoEditarActividad(actividad)
            },
            onLongClick = { actividad ->
                mostrarDialogoEliminar(actividad)
            }
        )
        rvNotas.adapter = adapter

        actividadViewModel.obtenerActividadesDeCurso(idCursoActual).observe(this) { actividades ->
            adapter.actualizarLista(actividades)

            var sumaPesos = 0f
            var sumaPuntos = 0f

            for (act in actividades) {
                sumaPesos += act.peso
                // Siempre dividir entre 100f porque 'act.calificacion' es base 100
                val puntosGanados = (act.calificacion / 100f) * act.peso
                sumaPuntos += puntosGanados
            }
            
            val currentScale = ScaleManager.getCurrentScale(this)
            val promedioVisual = ScaleManager.convert(sumaPuntos.toDouble(), currentScale)

            tvPesoTotal.text = getString(R.string.progreso_formato, String.format("%.0f", sumaPesos))
            tvCalificacionTotal.text = getString(R.string.acumulado_formato, promedioVisual)

            if (sumaPesos >= 100f) {
                tvPesoTotal.setTextColor(ContextCompat.getColor(this, R.color.verde_completo))
                tvPesoTotal.text = getString(R.string.materia_completa)
                tvCalificacionTotal.text = getString(R.string.final_formato, promedioVisual)
                tvCalificacionTotal.textSize = 22f
            } else {
                tvPesoTotal.setTextColor(android.graphics.Color.BLACK)
                tvCalificacionTotal.textSize = 18f
            }
        }

        fab.setOnClickListener {
            mostrarDialogoAgregarNota()
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (idCursoActual != -1) {
             adapter.notifyDataSetChanged()
             // También refrescar los cálculos totales
             actividadViewModel.obtenerActividadesDeCurso(idCursoActual).value?.let { actividades ->
                 var sumaPesos = 0f
                 var sumaPuntos = 0f
                 for (act in actividades) {
                    sumaPesos += act.peso
                    // Siempre dividir entre 100f porque 'act.calificacion' es base 100
                    val puntosGanados = (act.calificacion / 100f) * act.peso
                    sumaPuntos += puntosGanados
                 }
                 val currentScale = ScaleManager.getCurrentScale(this)
                 val promedioVisual = ScaleManager.convert(sumaPuntos.toDouble(), currentScale)
                 
                 val tvPesoTotal = findViewById<TextView>(R.id.tvPesoTotal)
                 val tvCalificacionTotal = findViewById<TextView>(R.id.tvCalificacionTotal)
                 
                 // Re-aplicar lógica de UI
                 if (sumaPesos >= 100f) {
                     tvPesoTotal.setTextColor(ContextCompat.getColor(this, R.color.verde_completo))
                     tvPesoTotal.text = getString(R.string.materia_completa)
                     tvCalificacionTotal.text = getString(R.string.final_formato, promedioVisual)
                     tvCalificacionTotal.textSize = 22f
                 } else {
                     tvPesoTotal.setTextColor(android.graphics.Color.BLACK)
                     tvPesoTotal.text = getString(R.string.progreso_formato, String.format("%.0f", sumaPesos))
                     tvCalificacionTotal.text = getString(R.string.acumulado_formato, promedioVisual)
                     tvCalificacionTotal.textSize = 18f
                 }
             }
        }
    }

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
        
        val currentScale = ScaleManager.getCurrentScale(this)
        val hint = ScaleManager.getHintForScale(currentScale)

        val inputNota = EditText(this)
        inputNota.hint = hint
        inputNota.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputNota)

        AlertDialog.Builder(this)
            .setTitle("Nueva Actividad")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString()
                val peso = inputPeso.text.toString().toFloatOrNull()
                val notaStr = inputNota.text.toString()
                
                val notaInterna = ScaleManager.parseGradeInput(notaStr, currentScale)

                if (nombre.isNotEmpty() && peso != null && notaInterna != null) {
                    val nuevaActividad = Actividad(
                        id_curso = idCursoActual,
                        nombre = nombre,
                        peso = peso,
                        calificacion = notaInterna
                    )
                    actividadViewModel.insertarActividad(nuevaActividad)
                } else {
                    Toast.makeText(this, getString(R.string.datos_invalidos), Toast.LENGTH_SHORT).show()
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
        
        val currentScale = ScaleManager.getCurrentScale(this)
        // Convertir nota interna (0-100) a escala actual para mostrar al usuario
        val notaUsuario = ScaleManager.convert(actividad.calificacion.toDouble(), currentScale)

        val inputNota = EditText(this)
        inputNota.setText(notaUsuario)
        inputNota.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputNota)

        AlertDialog.Builder(this)
            .setTitle("Editar Actividad")
            .setView(layout)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevoNombre = inputNombre.text.toString()
                val nuevoPeso = inputPeso.text.toString().toFloatOrNull()
                val nuevaNotaStr = inputNota.text.toString()
                
                val nuevaNotaInterna = ScaleManager.parseGradeInput(nuevaNotaStr, currentScale)

                if (nuevoNombre.isNotEmpty() && nuevoPeso != null && nuevaNotaInterna != null) {
                    val actividadEditada = actividad.copy(
                        nombre = nuevoNombre,
                        peso = nuevoPeso,
                        calificacion = nuevaNotaInterna
                    )
                    actividadViewModel.actualizarActividad(actividadEditada)
                } else {
                    Toast.makeText(this, getString(R.string.datos_invalidos), Toast.LENGTH_SHORT).show()
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