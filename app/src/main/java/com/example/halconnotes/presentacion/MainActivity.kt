package com.example.halconnotes.presentacion

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.control.CursoViewModel
import com.example.halconnotes.data.Curso
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import android.text.InputFilter

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // 1. Instanciamos el ViewModel (El intermediario con la BD)
    private val cursoViewModel: CursoViewModel by viewModels()

    private lateinit var adapter: CursoAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var rvCursos: RecyclerView
    private lateinit var fab: FloatingActionButton

    // Filtro para Módulo 1: Letras, Números y Espacios (sin símbolos raros)
    private val filtroNombreMateria = InputFilter { source, start, end, _, _, _ ->
        for (i in start until end) {
            // Acepta si es letra, dígito o espacio en blanco
            if (!Character.isLetterOrDigit(source[i]) && !Character.isSpaceChar(source[i])) {
                return@InputFilter "" // Rechaza el carácter
            }
        }
        null // Acepta el carácter
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        fragmentContainer = findViewById(R.id.fragment_container)

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                fragmentContainer.visibility = View.VISIBLE
                rvCursos.visibility = View.GONE
                fab.visibility = View.GONE
                
                // ¡OCULTAR EL TOOLBAR PRINCIPAL!
                toolbar.visibility = View.GONE 
            } else {
                fragmentContainer.visibility = View.GONE
                rvCursos.visibility = View.VISIBLE
                fab.visibility = View.VISIBLE

                toolbar.visibility = View.VISIBLE
                
                if (::adapter.isInitialized) {
                    adapter.notifyDataSetChanged()
                }
            }
        }

        // Listener para el menú lateral
        navView.setNavigationItemSelectedListener(this)

        rvCursos = findViewById(R.id.rvCursos)
        rvCursos.layoutManager = LinearLayoutManager(this)

        // 2. Configuramos el adapter
        adapter = CursoAdapter(
            onCursoClick = { posicion ->
                val curso = adapter.obtenerCurso(posicion)
                val intent = Intent(this, NotasActivity::class.java)
                intent.putExtra("NOMBRE_CURSO", curso.nombre)
                intent.putExtra("ID_CURSO", curso.id_curso)
                startActivity(intent)
            },
            onCursoLongClick = { posicion ->
                mostrarOpcionesCurso(posicion)
            }
        )
        rvCursos.adapter = adapter

        cursoViewModel.todosLosCursos.observe(this) { cursosCargados ->
            adapter.actualizarLista(cursosCargados)

            val menuGrafica = findViewById<NavigationView>(R.id.nav_view).menu.findItem(R.id.nav_grafica)
            menuGrafica?.isEnabled = !cursosCargados.isNullOrEmpty()
        }

        fab = findViewById(R.id.fabAgregar)
        fab.setOnClickListener {
            mostrarDialogoAgregar()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_promedio -> {
                val intent = Intent(this, PromedioActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_grafica -> {
                val cursos = adapter.obtenerTodosLosCursos()
                if (cursos.isNotEmpty()) {
                    val nombres = ArrayList<String>()
                    val promedios = FloatArray(cursos.size)
                    cursos.forEachIndexed { index, curso ->
                        nombres.add(curso.nombre)
                        promedios[index] = curso.promedioActual
                    }
                    val intent = Intent(this, GraficaActivity::class.java)
                    intent.putStringArrayListExtra("NOMBRES", nombres)
                    intent.putExtra("PROMEDIOS", promedios)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No hay datos para graficar", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_grade_scale -> {
                mostrarFragmentoConfiguracion()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun mostrarFragmentoConfiguracion() {
        rvCursos.visibility = View.GONE
        fab.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GradeScaleFragment())
            .addToBackStack(null) // Importante para que el listener funcione
            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (supportFragmentManager.backStackEntryCount > 0) {
             // Deja que el sistema maneje el popBackStack, lo que disparará nuestro listener
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun mostrarDialogoAgregar() {
        val input = EditText(this)
        input.hint = "Ej. Programación Lógica"

        input.filters = arrayOf(
            InputFilter.LengthFilter(30), // Máximo 30 caracteres
            filtroNombreMateria           // Solo letras, números y espacios
        )

        AlertDialog.Builder(this)
            .setTitle("Nueva Materia")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = input.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    val nuevoCurso = Curso(id_alumno = 1, nombre = nombre)
                    cursoViewModel.insertarCurso(nuevoCurso)
                } else {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoEditar(posicion: Int) {
        val cursoActual = adapter.obtenerCurso(posicion)
        val input = EditText(this)
        input.setText(cursoActual.nombre)

        input.filters = arrayOf(
            InputFilter.LengthFilter(30), // Máximo 30 caracteres
            filtroNombreMateria           // Solo letras, números y espacios
        )

        AlertDialog.Builder(this)
            .setTitle("Renombrar Materia")
            .setView(input)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevoNombre = input.text.toString().trim()
                if (nuevoNombre.isNotEmpty()) {
                    val cursoEditado = cursoActual.copy(nombre = nuevoNombre)
                    cursoViewModel.actualizarCurso(cursoEditado)
                } else {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
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
