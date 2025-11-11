package me.andilin.runwithme

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class MapData(
    @DocumentId
    val id: String = "",
    val userId: String = "", // ID del usuario que guarda la ubicación
    val userName: String = "",
    val location: MapLocation = MapLocation(),
    val activityType: String = "Running", // Tipo de actividad
    val timestamp: Date = Date(),
    val note: String = "" // Nota opcional del usuario
)

data class MapLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val city: String = "",
    val country: String = ""
)