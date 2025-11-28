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
        holder.tvPromedio.text = String.format("%.2f", curso.promedioActual)
    }

    override fun getItemCount() = lista.size

    fun actualizar(listaNueva: List<Curso>) {
        lista = listaNueva
        notifyDataSetChanged()
    }
}

