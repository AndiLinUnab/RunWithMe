package me.andilin.runwithme.model
data class Publicacion(
    val id: String = "",
    val autor: String = "",
    val texto: String = "",
    val grupo: String = "",
    val imagenPath: String? = null,
    val tiempo: String = "",
    val userId: String = ""
)