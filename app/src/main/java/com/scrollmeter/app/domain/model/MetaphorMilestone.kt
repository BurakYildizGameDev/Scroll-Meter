package com.scrollmeter.app.domain.model

data class MetaphorMilestone(
    val id: String,
    val name: String,
    val targetMeters: Double,
    val iconEmoji: String,
    val description: String
)

data class MetaphorProgress(
    val currentMilestone: MetaphorMilestone,
    val nextMilestone: MetaphorMilestone?,
    val remainingMetersToNext: Double,
    val progressFractionToNext: Float
)
