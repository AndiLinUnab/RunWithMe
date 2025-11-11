package me.andilin.runwithme.model

data class UserData(
    val nombre: String = "",
    val correo: String = "",
    val fechaNacimiento: String = "",
    val genero: String = "",
    val nivel: String = "",
    val fotoLocal: String? = null
)

