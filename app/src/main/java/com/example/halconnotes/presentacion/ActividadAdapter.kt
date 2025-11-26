package com.example.halconnotes.presentacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.halconnotes.R
import com.example.halconnotes.data.Actividad

class ActividadAdapter(
    // Recibimos dos acciones:
    // 1. onItemClick -> Para EDITAR (Clic normal)
    // 2. onLongClick -> Para ELIMINAR (Clic largo)
    private val onItemClick: (Actividad) -> Unit,
    private val onLongClick: (Actividad) -> Unit
) : RecyclerView.Adapter<ActividadAdapter.ActividadViewHolder>() {

    private var listaActividades = emptyList<Actividad>()

    class ActividadViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Referencias a los elementos del diseño item_nota.xml
        val tvNombre: TextView = view.findViewById(R.id.tvNombreActividad)
        val tvCalificacion: TextView = view.findViewById(R.id.tvCalificacion) // Texto Grande Azul
        val tvPeso: TextView = view.findViewById(R.id.tvPeso) // Texto Pequeño Gris
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActividadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nota, parent, false)
        return ActividadViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActividadViewHolder, position: Int) {
        val actividad = listaActividades[position]

        // 1. Mostrar el Nombre
        holder.tvNombre.text = actividad.nombre

        // 2. Cálculo visual de puntos ganados
        // Fórmula: (Calificación / 10) * Peso
        // Ejemplo: (9.5 / 10) * 20% = 1.9 puntos reales
        val puntosGanados = (actividad.calificacion / 10f) * actividad.peso

        // Formato a 2 decimales para que se vea limpio (ej. "1.90")
        val puntosFormateados = String.format("%.2f", puntosGanados)

        // 3. Asignar textos
        // EN GRANDE (AZUL): Puntos reales que suman al promedio
        holder.tvCalificacion.text = "$puntosFormateados pts"

        // EN PEQUEÑO (GRIS): Detalle de cuánto sacó y cuánto valía
        holder.tvPeso.text = "Sacaste: ${actividad.calificacion}  (Peso: ${actividad.peso}%)"

        // 4. Configurar CLIC NORMAL -> Editar
        holder.itemView.setOnClickListener {
            onItemClick(actividad)
        }

        // 5. Configurar CLIC LARGO -> Eliminar
        holder.itemView.setOnLongClickListener {
            onLongClick(actividad)
            true // 'true' indica que ya manejamos el evento
        }
    }

    override fun getItemCount() = listaActividades.size

    // Función para actualizar la lista desde la Activity
    fun actualizarLista(nuevasActividades: List<Actividad>) {
        this.listaActividades = nuevasActividades
        notifyDataSetChanged()
    }
}