package com.example.halconnotes.control

import android.content.Context
import android.content.SharedPreferences
import com.example.halconnotes.presentacion.GradeScaleFragment

object EscalaManager {

    fun getCurrentScale(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(GradeScaleFragment.PREFS_NAME, Context.MODE_PRIVATE)
        // Default a 0 a 100 si no existe
        return prefs.getString(GradeScaleFragment.KEY_SELECTED_SCALE, "Escala: 0 a 100 (Estándar)") ?: "Escala: 0 a 100 (Estándar)"
    }
    
    fun saveScalePreference(context: Context, scale: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(GradeScaleFragment.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(GradeScaleFragment.KEY_SELECTED_SCALE, scale).apply()
    }


    fun getNumericScaleMax(scale: String): Float {
        return when {
            scale.contains("0 a 5") -> 5.0f
            scale.contains("A a F") -> 100f // Base interna 100

            else -> 100f 
        }
    }

    fun convert(value0to100: Double, scaleType: String): String {
        // Protección contra valores nulos o infinitos
        if (value0to100.isNaN()) return "0.0"

        return when {
            scaleType.contains("0 a 5") -> {
                // Regla de 3: (Valor / 100) * 5
                val gpa = (value0to100 / 100.0) * 5.0
                String.format("%.1f", gpa)
            }
            scaleType.contains("A a F") -> {
                when {
                    value0to100 >= 90 -> "A"
                    value0to100 >= 80 -> "B"
                    value0to100 >= 70 -> "C"
                    value0to100 >= 60 -> "D"
                    else -> "F"
                }
            }
            else -> { // Caso 0 a 100
                // Simplemente mostramos el valor base tal cual. NO multiplicar ni dividir.
                String.format("%.1f", value0to100)
            }
        }
    }
    
    // Helpers para inputs (EditText)
    fun getHintForScale(scale: String): String {
        return if (scale.contains("0 a 5")) "Calificación (0-5)" else "Calificación (0-100)"
    }
    
    fun parseGradeInput(input: String, scale: String): Float? {
        val value = input.toFloatOrNull() ?: return null
        
        // Si el usuario está en escala 0-5, convertimos su entrada a base 100 para guardar en BD
        if (scale.contains("0 a 5")) {
            return if (value in 0f..5f) (value / 5f) * 100f else null
        }
        
        // Si es 0-100
        return if (value in 0f..100f) value else null
    }
    
    // Función de compatibilidad para el adaptador
    fun formatGrade(grade0to10: Float, scale: String): String {
         return convert(grade0to10.toDouble(), scale)
    }
}