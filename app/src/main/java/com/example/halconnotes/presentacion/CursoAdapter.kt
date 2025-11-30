package com.example.halconnotes.presentacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.control.ScaleManager
import com.example.halconnotes.data.Curso

class CursoAdapter(
    // Ya no recibimos la lista en el constructor, la inicializamos vacía
    private val onCursoClick: (Int) -> Unit,
    private val onCursoLongClick: (Int) -> Unit
) : RecyclerView.Adapter<CursoAdapter.CursoViewHolder>() {

    // La lista ahora es una variable interna
    private var listaCursos = emptyList<Curso>()
    
    // Flag para verificar si el adaptador está inicializado (útil en MainActivity)
    val isInitialized: Boolean = true

    class CursoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCurso)
        val tvPromedio: TextView = view.findViewById(R.id.tvPromedioCurso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CursoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_curso, parent, false)
        return CursoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CursoViewHolder, position: Int) {
        val curso = listaCursos[position]
        holder.tvNombre.text = curso.nombre
        
        // 1. Obtener la escala ACTUALIZADA desde las preferencias
        // Es vital leerla aquí para que cada vez que se redibuje la lista (notifyDataSetChanged), tome el valor nuevo.
        val context = holder.itemView.context
        val currentScale = ScaleManager.getCurrentScale(context)
        
        // 2. Convertir el promedio base (0-100) a la escala visual
        val promedioVisual = ScaleManager.convert(curso.promedioActual.toDouble(), currentScale)
        
        // 3. Mostrar el valor convertido
        holder.tvPromedio.text = promedioVisual

        // Listeners (mantener igual)
        holder.itemView.setOnClickListener { onCursoClick(position) }
        holder.itemView.setOnLongClickListener {
            onCursoLongClick(position)
            true
        }
    }

    override fun getItemCount() = listaCursos.size

    // Metodo para obtener el curso en una posición específica (útil para clicks)
    fun obtenerCurso(posicion: Int): Curso {
        return listaCursos[posicion]
    }

    // ¡NUEVO! Función para actualizar los datos desde la Base de Datos
    fun actualizarLista(nuevosCursos: List<Curso>) {
        this.listaCursos = nuevosCursos
        notifyDataSetChanged() // Refresca la vista
    }

    // Devuelve todos los cursos actualmente cargados en el adapter
    fun obtenerTodosLosCursos(): List<Curso> {
        return listaCursos
    }

}