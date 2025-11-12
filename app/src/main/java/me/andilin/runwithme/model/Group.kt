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
    val experienceLevel: String = "",
    val sportType: String = "",
    val distance: Float = 15f,
    val trainingDays: List<String> = emptyList(),
    val meetingTime: String = "",
    val createdBy: String = "",
    val createdAt: Date = Date(),
    val members: List<String> = emptyList(),
    val memberCount: Int = 1,

    val pace: Double = 10.toDouble()

)

data class GroupLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val useCurrentLocation: Boolean = true,
    val address: String = ""
)