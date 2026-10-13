package com.scrollmeter.app.domain.model

enum class AchievementCategory {
    MILESTONE,
    HABIT,
    SPECIALIST,
    PRECISION
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val category: AchievementCategory,
    val isUnlocked: Boolean,
    val progress: Float, // 0.0f to 1.0f
    val progressText: String
)
