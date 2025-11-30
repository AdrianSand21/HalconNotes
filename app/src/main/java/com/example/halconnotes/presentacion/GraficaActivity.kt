package com.example.halconnotes.presentacion

    import androidx.appcompat.app.AppCompatActivity
    import android.os.Bundle
    import com.example.halconnotes.R
    import com.github.mikephil.charting.charts.BarChart
    import com.github.mikephil.charting.data.BarData
    import com.github.mikephil.charting.data.BarDataSet
    import com.github.mikephil.charting.data.BarEntry

    class GraficaActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_grafica)

            val barChart = findViewById<BarChart>(R.id.barChart)

            // ------- DATOS DE EJEMPLO -------- //
            val entries = ArrayList<BarEntry>()
            entries.add(BarEntry(1f, 80f))  // Parcial 1
            entries.add(BarEntry(2f, 90f))  // Parcial 2
            entries.add(BarEntry(3f, 75f))  // Parcial 3

            val dataSet = BarDataSet(entries, "Progreso de calificaciones")
            val data = BarData(dataSet)

            barChart.data = data
            barChart.invalidate() // refresca el gráfico
        }
    }


