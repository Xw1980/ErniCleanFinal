package com.example.erniclean

data class Formulario(
    val direccion: String = "",
    val email: String = "",
    val mensaje: String = "",
    val nombre: String = "",
    val serviciosSeleccionados: List<String> = emptyList(),
    val telefono: String = ""
)