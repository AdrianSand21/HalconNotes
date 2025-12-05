package com.example.halconnotes.presentacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.control.EscalaManager // <--- Asegúrate de tener este import
import com.example.halconnotes.data.Actividad

class ActividadAdapter(
    // Recibimos dos acciones: Editar (Click) y Eliminar (LongClick)
    private val onItemClick: (Actividad) -> Unit,
    private val onLongClick: (Actividad) -> Unit
) : RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder>() {

    private var listaActividades = emptyList<Actividad>()

    // Variable para guardar la escala actual (Por defecto 0-100)
    private var escalaActual: String = "Escala: 0 a 100 (Estándar)"

    class ActividadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreActividad)
        val tvCalificacion: TextView = view.findViewById(R.id.tvCalificacion)
        val tvPeso: TextView = view.findViewById(R.id.tvPeso)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nota, parent, false)
        return ActividadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val actividad = listaActividades[position]

        holder.tvNombre.text = actividad.nombre

        // 1. Cálculo de puntos ganados (Base 100) para el texto grande
        val puntosGanados = (actividad.calificacion / 100f) * actividad.peso
        val puntosFormateados = String.format("%.2f", puntosGanados)
        holder.tvCalificacion.text = "$puntosFormateados pts"

        // 2. Cálculo visual de la nota original según la escala
        val notaVisual = EscalaManager.convert(actividad.calificacion.toDouble(), escalaActual)

        // Texto pequeño
        holder.tvPeso.text = "Sacaste: $notaVisual  (Valía ${actividad.peso}%)"

        // Listeners
        holder.itemView.setOnClickListener { onItemClick(actividad) }
        holder.itemView.setOnLongClickListener {
            onLongClick(actividad)
            true
        }
    }

    override fun getItemCount() = listaActividades.size

    fun actualizarLista(nuevasActividades: List<Actividad>) {
        this.listaActividades = nuevasActividades
        notifyDataSetChanged()
    }

    fun setEscala(nuevaEscala: String) {
        this.escalaActual = nuevaEscala
        // Al cambiar la escala, refrescamos la lista para que los números cambien (ej. de 80 a 4.0)
        notifyDataSetChanged()
    }
}