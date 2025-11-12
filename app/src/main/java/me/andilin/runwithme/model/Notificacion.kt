package me.andilin.runwithme.model

data class Notificacion(
    val id: String = "",
    val tipo: String = "", // "like", "comentario", "grupo", "seguidor", "evento"
    val titulo: String = "",
    val mensaje: String = "",
    val tiempo: String = "",
    val leida: Boolean = false,
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val usuarioFoto: String = "",
    val timestamp: Long = 0L
)