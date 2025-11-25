package com.example.halconnotes

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NotasActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notas)

        // Recibir el nombre de la materia desde el MainActivity
        val nombreCurso = intent.getStringExtra("NOMBRE_CURSO") ?: "Materia"

        // Poner el título
        val tvTitulo = findViewById<TextView>(R.id.tvTituloMateria)
        tvTitulo.text = nombreCurso

        // AQUI HAREMOS LA LOGICA DE AGREGAR NOTAS DESPUÉS
    }
}