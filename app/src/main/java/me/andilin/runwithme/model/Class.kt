package me.andilin.runwithme.model

data class UserData(
    val nombre: String = "",
    val correo: String = "",
    val fechaNacimiento: String = "",
    val genero: String = "",
    val nivel: String = "",
    val fotoLocal: String? = null
)
data class Publicacion(
    val id: String = "",
    val autor: String = "",
    val texto: String = "",
    val grupo: String = "",
    val imagenPath: String? = null,
    val tiempo: String = ""
)
