package com.example.halconnotes.presentacion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.halconnotes.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class GraficaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grafica)

        val barChart = findViewById<BarChart>(R.id.barChart)

        // --- RECIBIR DATOS DESDE MAINACTIVITY ---
        val nombres = intent.getStringArrayListExtra("NOMBRES") ?: arrayListOf()
        val promedios = intent.getFloatArrayExtra("PROMEDIOS") ?: floatArrayOf()

        // --- CREAR ENTRADAS PARA LA GRÁFICA ---
        val entries = ArrayList<BarEntry>()
        nombres.forEachIndexed { index, _ ->
            entries.add(BarEntry(index.toFloat(), promedios[index]))
        }

        // --- CONFIGURAR DATASET ---
        val dataSet = BarDataSet(entries, "Promedio por Curso")
        dataSet.color = resources.getColor(R.color.secondary, null) // Puedes cambiar el color

        val data = BarData(dataSet)
        data.barWidth = 0.9f

        // --- CONFIGURAR BARCHART ---
        barChart.data = data
        barChart.setFitBars(true)
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(nombres)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.isGranularityEnabled = true
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.axisMaximum = 100f
        barChart.axisRight.isEnabled = false
        barChart.description.text = "Promedio de calificaciones"
        barChart.animateY(1000)
        barChart.invalidate() // refresca el gráfico
    }
}



