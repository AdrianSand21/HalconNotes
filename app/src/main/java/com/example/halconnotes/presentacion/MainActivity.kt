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

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // 1. Instanciamos el ViewModel (El intermediario con la BD)
    private val cursoViewModel: CursoViewModel by viewModels()

    private lateinit var adapter: CursoAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var rvCursos: RecyclerView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicialización de vistas para navegación y drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        fragmentContainer = findViewById(R.id.fragment_container)

        // Configuración del Toolbar para abrir el Drawer
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Listener para ocultar el Toolbar principal cuando se muestra un fragmento
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                // ESTAMOS EN EL FRAGMENTO (Escala)
                fragmentContainer.visibility = View.VISIBLE
                rvCursos.visibility = View.GONE
                fab.visibility = View.GONE
                // items de menú ya se manejan por separado
                
                // ¡OCULTAR EL TOOLBAR PRINCIPAL!
                toolbar.visibility = View.GONE 
            } else {
                // ESTAMOS EN EL INICIO
                fragmentContainer.visibility = View.GONE
                rvCursos.visibility = View.VISIBLE
                fab.visibility = View.VISIBLE
                
                // ¡MOSTRAR EL TOOLBAR PRINCIPAL!
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

        // 3. OBSERVAMOS la Base de Datos
        cursoViewModel.todosLosCursos.observe(this) { cursosCargados ->
            adapter.actualizarLista(cursosCargados)

            // Habilitar el ítem del menú de la gráfica si la lista contiene datos
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
        // Forzamos al adaptador a redibujar la lista.
        // Como el adaptador lee la escala en 'onBindViewHolder', esto actualizará el formato (ej. de 100 a 5.0) instantáneamente.
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
        // Ocultar elementos de la vista principal
        rvCursos.visibility = View.GONE
        fab.visibility = View.GONE
        fragmentContainer.visibility = View.VISIBLE
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.visibility = View.GONE

        // Cargar el fragmento
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GradeScaleFragment())
            .addToBackStack(null) // Importante para que el listener funcione
            .commit()
    }

    // Manejo del botón atrás para cerrar el fragmento si está abierto
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
                    val nuevoCurso = Curso(id_alumno = 1, nombre = nombre)
                    cursoViewModel.insertarCurso(nuevoCurso)
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
