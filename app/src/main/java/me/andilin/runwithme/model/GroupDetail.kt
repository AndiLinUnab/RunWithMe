package me.andilin.runwithme.model

data class GroupDetail(
    val id: String,
    val name: String,
    val description: String,
    val experienceLevel: String,
    val sportType: String,
    val distance: Float,
    val trainingDays: List<String>,
    val meetingTime: String,
    val memberCount: Int,
    val members: List<String>,
    val createdBy: String
)