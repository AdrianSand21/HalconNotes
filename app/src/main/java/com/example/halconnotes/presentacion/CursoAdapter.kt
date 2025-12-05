package com.example.halconnotes.presentacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.control.EscalaManager
import com.example.halconnotes.data.Curso

class CursoAdapter(
    private val onCursoClick: (Int) -> Unit,
    private val onCursoLongClick: (Int) -> Unit
) : RecyclerView.Adapter<CursoAdapter.CursoViewHolder>() {

    private var listaCursos = emptyList<Curso>()
    
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
        val context = holder.itemView.context
        val currentScale = EscalaManager.getCurrentScale(context)
        
        // 2. Convertir el promedio base (0-100) a la escala visual
        val promedioVisual = EscalaManager.convert(curso.promedioActual.toDouble(), currentScale)
        
        // 3. Mostrar el valor convertido
        holder.tvPromedio.text = promedioVisual

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

    fun actualizarLista(nuevosCursos: List<Curso>) {
        this.listaCursos = nuevosCursos
        notifyDataSetChanged() // Refresca la vista
    }

    fun obtenerTodosLosCursos(): List<Curso> {
        return listaCursos
    }

}