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
import com.example.halconnotes.control.EscalaManager
import com.example.halconnotes.data.Actividad
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.text.InputFilter
class NotasActivity : AppCompatActivity() {

    private val actividadViewModel: ActividadViewModel by viewModels()
    private lateinit var adapter: ActividadAdapter
    private var idCursoActual: Int = -1
    private var pesoAcumuladoActual: Float = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notas)
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        val root = findViewById<android.view.View>(R.id.root_layout_notas)
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            toolbar.setPadding(0, systemBars.top, 0, 0)
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }
        val nombreCurso = intent.getStringExtra("NOMBRE_CURSO") ?: "Curso"
        idCursoActual = intent.getIntExtra("ID_CURSO", -1)
        if (idCursoActual == -1) {
            Toast.makeText(this, "Error al cargar el curso", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        toolbar.title = nombreCurso
        toolbar.setNavigationOnClickListener { finish() }

        val tvPesoTotal = findViewById<TextView>(R.id.tvPesoTotal)
        val tvCalificacionTotal = findViewById<TextView>(R.id.tvCalificacionTotal)
        val rvNotas = findViewById<RecyclerView>(R.id.rvNotas)
        val fab = findViewById<FloatingActionButton>(R.id.fabAgregarNota)

        rvNotas.layoutManager = LinearLayoutManager(this)

        // Obtenemos la escala actual guardada (Módulo 5)
        val escalaActual = EscalaManager.getCurrentScale(this)

        // Configurar Adapter
        adapter = ActividadAdapter(
            onItemClick = { actividad -> mostrarDialogoEditarActividad(actividad, escalaActual) },
            onLongClick = { actividad -> mostrarDialogoEliminar(actividad) }
        )
        // Le decimos al adaptador qué escala usar para mostrar los datos (Módulo 5)
        adapter.setEscala(escalaActual)
        rvNotas.adapter = adapter

        actividadViewModel.obtenerActividadesDeCurso(idCursoActual).observe(this) { actividades ->
            adapter.actualizarLista(actividades)

            var sumaPesos = 0f
            var sumaPuntos = 0f

            for (act in actividades) {
                sumaPesos += act.peso
                sumaPuntos += (act.calificacion / 100f) * act.peso
            }

            pesoAcumuladoActual = sumaPesos


            val promedioVisual = EscalaManager.convert(sumaPuntos.toDouble(), escalaActual)

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
            if (pesoAcumuladoActual >= 100f) {
                Toast.makeText(this, "El curso ya tiene el 100% de peso", Toast.LENGTH_SHORT).show()
            } else {
                mostrarDialogoAgregarNota(escalaActual)
            }
        }
    }

    // Necesario para refrescar si cambias la escala en configuración y vuelves
    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            val nuevaEscala = EscalaManager.getCurrentScale(this)
            adapter.setEscala(nuevaEscala)
            // Forzar refresh de los cálculos de totales (re-lanzar observer indirectamente o recalcular UI)
            // Nota: El observer de LiveData se encarga si la data cambia, pero si solo cambia la config
            // a veces es necesario notificar al adapter.
        }
    }

    private fun mostrarDialogoAgregarNota(escala: String) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputNombre = EditText(this)
        inputNombre.hint = "Nombre (Ej. Examen)"
        inputNombre.filters = arrayOf(
            InputFilter.LengthFilter(20),
            filtroAlfaNumericoConEspacios
        )
        layout.addView(inputNombre)

        val inputPeso = EditText(this)
        inputPeso.hint = "Peso % (Disponible: ${100 - pesoAcumuladoActual}%)"
        inputPeso.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputPeso)

        val inputNota = EditText(this)
        // El hint cambia según la escala (ej: "0-5" o "0-100") gracias a EscalaManager
        inputNota.hint = EscalaManager.getHintForScale(escala)
        inputNota.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputNota)

        AlertDialog.Builder(this)
            .setTitle("Nueva Actividad")
            .setView(layout)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = inputNombre.text.toString()
                val peso = inputPeso.text.toString().toFloatOrNull()
                val notaString = inputNota.text.toString()
                val notaNormalizada = EscalaManager.parseGradeInput(notaString, escala)
                if (nombre.isNotEmpty() && peso != null && notaNormalizada != null) {

                    if (pesoAcumuladoActual + peso > 100.1f) {
                        Toast.makeText(this, "Error: El peso total superaría el 100%", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    val nuevaActividad = Actividad(
                        id_curso = idCursoActual,
                        nombre = nombre,
                        peso = peso,
                        calificacion = notaNormalizada
                    )
                    actividadViewModel.insertarActividad(nuevaActividad)
                } else {
                    Toast.makeText(this, getString(R.string.datos_invalidos), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    private fun mostrarDialogoEditarActividad(actividad: Actividad, escala: String) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)

        val inputNombre = EditText(this)
        inputNombre.setText(actividad.nombre)
        inputNombre.filters = arrayOf(
            InputFilter.LengthFilter(20),
            filtroAlfaNumericoConEspacios
        )
        layout.addView(inputNombre)

        val inputPeso = EditText(this)
        inputPeso.setText(actividad.peso.toString())
        inputPeso.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputPeso)

        val inputNota = EditText(this)
        val notaVisual = EscalaManager.convert(actividad.calificacion.toDouble(), escala)
        inputNota.setText(notaVisual)
        inputNota.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputNota)

        AlertDialog.Builder(this)
            .setTitle("Editar Actividad")
            .setView(layout)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevoNombre = inputNombre.text.toString()
                val nuevoPeso = inputPeso.text.toString().toFloatOrNull()
                val nuevaNotaStr = inputNota.text.toString()
                val nuevaNotaNormalizada = EscalaManager.parseGradeInput(nuevaNotaStr, escala)
                if (nuevoNombre.isNotEmpty() && nuevoPeso != null && nuevaNotaNormalizada != null) {
                    val pesoDiferencia = nuevoPeso - actividad.peso
                    if (pesoAcumuladoActual + pesoDiferencia > 100.1f) {
                        Toast.makeText(this, "Error: El nuevo peso supera el 100%", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val actividadEditada = actividad.copy(
                        nombre = nuevoNombre,
                        peso = nuevoPeso,
                        calificacion = nuevaNotaNormalizada
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

    private val filtroAlfaNumericoConEspacios = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) {
            if (!Character.isLetterOrDigit(source[i]) && !Character.isSpaceChar(source[i])) {
                return@InputFilter ""
            }
        }
        null
    }
}