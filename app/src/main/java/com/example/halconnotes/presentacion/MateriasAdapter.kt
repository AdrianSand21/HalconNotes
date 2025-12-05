package com.example.halconnotes.presentacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.data.Curso

class MateriasAdapter(
    private var lista: List<Curso> = emptyList()
) : RecyclerView.Adapter<MateriasAdapter.MateriaViewHolder>() {

    class MateriaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreCurso)
        val tvPromedio: TextView = view.findViewById(R.id.tvPromedioCurso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MateriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_curso, parent, false)
        return MateriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MateriaViewHolder, position: Int) {
        val curso = lista[position]
        holder.tvNombre.text = curso.nombre

        // 1. Obtener la escala configurada actualmente
        val context = holder.itemView.context
        val currentScale = com.example.halconnotes.control.EscalaManager.getCurrentScale(context)

        // 2. Convertir el promedio base (0-100) a la escala visual (0-5, A-F, etc.)
        val promedioVisual = com.example.halconnotes.control.EscalaManager.convert(
            curso.promedioActual.toDouble(), 
            currentScale
        )
        
        // 3. Mostrar el valor ya convertido
        holder.tvPromedio.text = promedioVisual
    }

    override fun getItemCount() = lista.size

    fun actualizar(listaNueva: List<Curso>) {
        lista = listaNueva
        notifyDataSetChanged()
    }
}