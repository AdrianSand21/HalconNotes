package com.example.halconnotes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CursoAdapter(
    private val lista: List<Curso>,
    private val onCursoClick: (Int) -> Unit,      // Clic normal (Abrir Módulo 2)
    private val onCursoLongClick: (Int) -> Unit   // Clic largo (Opciones Módulo 1)
) : RecyclerView.Adapter<CursoAdapter.CursoViewHolder>() {

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
        val curso = lista[position]
        holder.tvNombre.text = curso.nombre
        holder.tvPromedio.text = curso.promedio.toString()

        // 1. CLIC NORMAL (Abrir Notas)
        holder.itemView.setOnClickListener {
            onCursoClick(position)
        }

        // 2. CLIC LARGO (Editar/Eliminar)
        holder.itemView.setOnLongClickListener {
            onCursoLongClick(position)
            true
        }
    }

    override fun getItemCount() = lista.size
}