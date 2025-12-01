package com.example.halconnotes.presentacion

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.halconnotes.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.appbar.MaterialToolbar

class GraficaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grafica)

        // --- EDGE-TO-EDGE CONFIGURATION ---
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

        // --- TOOLBAR ---
        toolbar.setNavigationOnClickListener { finish() }

        // --- TÍTULO DE LA GRÁFICA ---
        val tituloGrafica = findViewById<TextView>(R.id.tvTituloGrafica)
        tituloGrafica.text = "Gráfica de calificaciones"
        tituloGrafica.setTextColor(Color.parseColor("#0D47A1")) // Azul intenso
        tituloGrafica.textSize = 26f // Ajusta tamaño para igualar percepción
        tituloGrafica.elevation = 4f

        // --- BARCHART ---
        val barChart = findViewById<BarChart>(R.id.barChart)

        // --- RECIBIR DATOS DESDE MAINACTIVITY ---
        val nombres = intent.getStringArrayListExtra("NOMBRES") ?: arrayListOf()
        val promedios = intent.getFloatArrayExtra("PROMEDIOS") ?: floatArrayOf()

        // --- CREAR ENTRADAS PARA LA GRÁFICA ---
        val entries = ArrayList<BarEntry>()
        nombres.forEachIndexed { index, _ ->
            entries.add(BarEntry(index.toFloat(), promedios.getOrElse(index) { 0f }))
        }

        // --- CONFIGURAR DATASET ---
        val dataSet = BarDataSet(entries, "Promedio por Curso")
        dataSet.color = Color.parseColor("#0D47A1") // mismo azul que el título
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
        barChart.description.isEnabled = false // opcional: quitar descripción de default
        barChart.animateY(1000)
        barChart.invalidate() // refresca el gráfico
    }
}