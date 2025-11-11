// Group.kt
package me.andilin.runwithme.model

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Group(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val location: GroupLocation = GroupLocation(),
    val experienceLevel: String = "Principiante",
    val sportType: String = "Running",
    val distance: Float = 15f,
    val trainingDays: List<String> = emptyList(),
    val meetingTime: String = "18:00",
    val createdBy: String = "", // ID del usuario que creó el grupo
    val createdAt: Date = Date(),
    val members: List<String> = emptyList(), // Solo IDs de miembros
    val memberCount: Int = 1,

    val pace: Double = 10.toDouble()

)

data class GroupLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val useCurrentLocation: Boolean = true,
    val address: String = ""
)