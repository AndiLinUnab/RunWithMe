package me.andilin.runwithme.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

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
    val tiempo: String = "",
    val userId: String = ""
)
data class Historia(
    val id: String = "",
    val autor: String = "",
    val imagenPath: String? = null,
    val tiempo: String = ""
)
data class Comentario(
    @DocumentId val id: String = "",
    val publicacionId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhoto: String? = null,
    val texto: String = "",
    val fecha: Date = Date(),
    val userEmail: String = ""
)