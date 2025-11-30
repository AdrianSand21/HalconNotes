package com.example.halconnotes.presentacion

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.halconnotes.R

class GradeScaleFragment : Fragment() {

    private lateinit var spinnerGradeScale: Spinner
    private lateinit var tvCurrentScale: TextView
    private lateinit var btnBack: ImageButton

    companion object {
        const val PREFS_NAME = "AjustesDeCalificaciones"
        const val KEY_SELECTED_SCALE = "ESCALA_SELECCIONADA"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_grade_scale, container, false)

        spinnerGradeScale = view.findViewById(R.id.spinner_grade_scale)
        tvCurrentScale = view.findViewById(R.id.text_view_current_scale)
        btnBack = view.findViewById(R.id.button_back_to_main)

        setupSpinner()
        setupNavigation()

        return view
    }

    private fun setupSpinner() {
        // 1. Obtener opciones y agregar el Prompt al inicio
        val opciones = resources.getStringArray(R.array.grade_scale_options).toMutableList()
        opciones.add(0, "Elige una opción") // Agregamos el texto por defecto al inicio

        // 2. Configurar adaptador con el layout personalizado (Texto Negro)
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item_custom, opciones)
        adapter.setDropDownViewResource(R.layout.spinner_item_custom)
        
        spinnerGradeScale.adapter = adapter

        // Cargar selección guardada y restaurar posición
        val prefs: SharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedScale = prefs.getString(KEY_SELECTED_SCALE, null)
        
        if (savedScale != null) {
            val position = adapter.getPosition(savedScale)
            if (position >= 0) {
                spinnerGradeScale.setSelection(position)
                tvCurrentScale.text = "Escala actual: $savedScale"
            }
        }

        // 3. Manejar la selección para ignorar la opción "Elige una opción"
        spinnerGradeScale.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val seleccion = parent.getItemAtPosition(position).toString()
                
                // Si selecciona el prompt, no hacemos nada (o mostramos error si intenta guardar)
                if (seleccion == "Elige una opción") {
                    tvCurrentScale.text = "Escala actual: Ninguna seleccionada"
                    return
                }

                // Verificar si cambió respecto a lo guardado para evitar Toast al iniciar
                val currentSaved = prefs.getString(KEY_SELECTED_SCALE, null)
                if (seleccion != currentSaved) {
                    saveScalePreference(seleccion)
                } else {
                     tvCurrentScale.text = "Escala actual: $seleccion"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun saveScalePreference(scale: String) {
        val prefs: SharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_SELECTED_SCALE, scale)
        editor.apply()

        tvCurrentScale.text = "Escala actual: $scale"
        Toast.makeText(requireContext(), "Escala guardada: $scale", Toast.LENGTH_SHORT).show()
    }

    private fun setupNavigation() {
        btnBack.setOnClickListener {
            // Priorizar navegación segura
            if (activity is MainActivity) {
                 // Como estamos manejando fragmentos manualmente en MainActivity
                 requireActivity().onBackPressed()
            } else {
                // Fallback genérico
                 requireActivity().onBackPressed()
            }
        }
    }
}
