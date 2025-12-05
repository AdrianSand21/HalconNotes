package com.example.halconnotes.presentacion

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.halconnotes.R
import com.example.halconnotes.control.EscalaManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.MaterialToolbar

class GraficaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grafica)

        // EDGE-TO-EDGE
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

        val root = findViewById<android.view.View>(R.id.root_layout_grafica)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())

            // 1. Arriba: Padding solo al Toolbar
            toolbar.setPadding(0, systemBars.top, 0, 0)

            // 2. Abajo: Padding al contenedor raíz para proteger el contenido inferior
            view.setPadding(0, 0, 0, systemBars.bottom)

            insets
        }

        // Toolbar
        toolbar.setNavigationOnClickListener { finish() }

        // Titulo (grafica)
        val tituloGrafica = findViewById<TextView>(R.id.tvTituloGrafica)
        tituloGrafica.text = "Gráfica de calificaciones"
        tituloGrafica.setTextColor(Color.parseColor("#0D47A1")) // Azul intenso
        tituloGrafica.textSize = 26f // Ajusta tamaño para igualar percepción
        tituloGrafica.elevation = 4f

        // Barchar
        val barChart = findViewById<BarChart>(R.id.barChart)

        // Obtener escala actual
        val currentScale = EscalaManager.getCurrentScale(this)
        var maxScale = EscalaManager.getNumericScaleMax(currentScale)
        val isAlfabetica = currentScale.contains("A a F")

        // Si es alfabética, usamos escala GPA de 4.0 para la gráfica
        if (isAlfabetica) {
            maxScale = 4f
        }

        // Recepción de datos desde mainActivity
        val nombres = intent.getStringArrayListExtra("NOMBRES") ?: arrayListOf()
        val promedios = intent.getFloatArrayExtra("PROMEDIOS") ?: floatArrayOf()

        // Creación de entradas para grafica
        val entries = ArrayList<BarEntry>()
        nombres.forEachIndexed { index, _ ->
            var valor = promedios.getOrElse(index) { 0f }

            // Si la escala es 0-5, convertir el valor (asumiendo que viene en 0-100)

            if (maxScale == 5.0f && !isAlfabetica) {
                valor = (valor / 100f) * 5f
            } else if (isAlfabetica) {
                // Convertir 0-100 a GPA 0-4
                valor = when {
                    valor >= 90 -> 4f // A
                    valor >= 80 -> 3f // B
                    valor >= 70 -> 2f // C
                    valor >= 60 -> 1f // D
                    else -> 0f        // F
                }
            }

            //Valores de las barras
            (barChart.data?.getDataSetByIndex(0) as? BarDataSet)?.valueFormatter =
                object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (isAlfabetica) {
                            return when (kotlin.math.round(value).toInt()) {
                                4 -> "A"
                                3 -> "B"
                                2 -> "C"
                                1 -> "D"
                                0 -> "F"
                                else -> ""
                            }
                        } else {
                            // Escala numérica: mostrar valor con 1 decimal
                            String.format("%.1f", value)
                        }
                    }
                }
            entries.add(BarEntry(index.toFloat(), valor))
        }

        // Configuración de dataset
        val dataSet = BarDataSet(entries, "Promedio por Curso")
        dataSet.color = Color.parseColor("#0D47A1") // mismo azul que el título
        val data = BarData(dataSet)
        data.barWidth = 0.9f

        // configuración de barchart
        barChart.data = data
        barChart.setFitBars(true)
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(nombres)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.isGranularityEnabled = true
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.axisMaximum = maxScale

        // Si es alfabética, formatear el eje Y con letras

        if (isAlfabetica) {
            barChart.axisLeft.granularity = 1f
            barChart.axisLeft.labelCount = 5
            barChart.axisLeft.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when (kotlin.math.round(value).toInt()) {

                        4 -> "A"
                        3 -> "B"
                        2 -> "C"
                        1 -> "D"
                        0 -> "F"
                        else -> ""

                    }

                }

            }
        } else {

            // Restaurar formateador por defecto si no es alfabética (por si acaso se reusa la vista)

            barChart.axisLeft.valueFormatter = null
        }

        //Valores encima de las barras

        (barChart.data?.getDataSetByIndex(0) as? BarDataSet)?.valueFormatter =
            object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when (kotlin.math.round(value).toInt()) {
                        4 -> "A"
                        3 -> "B"
                        2 -> "C"
                        1 -> "D"
                        0 -> "F"
                        else -> ""
                    }
                }
            }

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false // opcional: quitar descripción de default
        barChart.animateY(1000)
        barChart.invalidate() // refresca el gráfico

    }
}