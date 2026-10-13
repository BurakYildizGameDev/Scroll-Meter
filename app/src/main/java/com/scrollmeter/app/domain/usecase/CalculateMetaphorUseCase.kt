package com.scrollmeter.app.domain.usecase

import com.scrollmeter.app.domain.model.MetaphorMilestone
import com.scrollmeter.app.domain.model.MetaphorProgress
import javax.inject.Inject

class CalculateMetaphorUseCase @Inject constructor() {

    val milestones: List<MetaphorMilestone> = listOf(
        MetaphorMilestone(
            id = "ant",
            name = "Karınca Parkuru",
            targetMeters = 0.0,
            iconEmoji = "🐜",
            description = "Güne yeni başladın, parmağın henüz ısınıyor!"
        ),
        MetaphorMilestone(
            id = "giraffe",
            name = "Zürafa Boyu",
            targetMeters = 5.0,
            iconEmoji = "🦒",
            description = "Tebrikler! Bir zürafanın boyu kadar kaydırdın!"
        ),
        MetaphorMilestone(
            id = "bus",
            name = "Belediye Otobüsü",
            targetMeters = 15.0,
            iconEmoji = "🚌",
            description = "Parmağın koca bir belediye otobüsü boyu yol kat etti!"
        ),
        MetaphorMilestone(
            id = "football_pitch",
            name = "Futbol Sahası",
            targetMeters = 100.0,
            iconEmoji = "⚽",
            description = "Neredeyse bir futbol sahasını boydan boya koştun!"
        ),
        MetaphorMilestone(
            id = "statue_of_liberty",
            name = "Özgürlük Heykeli",
            targetMeters = 93.0,
            iconEmoji = "🗽",
            description = "Özgürlük Heykeli'nin meşalesine ulaştın!"
        ),
        MetaphorMilestone(
            id = "eiffel_tower",
            name = "Eyfel Kulesi",
            targetMeters = 330.0,
            iconEmoji = "🗼",
            description = "Eyfel Kulesi'nin zirvesindesin! Muhteşem manzara!"
        ),
        MetaphorMilestone(
            id = "burj_khalifa",
            name = "Burj Khalifa",
            targetMeters = 828.0,
            iconEmoji = "🏙️",
            description = "Dünyanın en yüksek binasını parmağınla fethettin!"
        ),
        MetaphorMilestone(
            id = "everest",
            name = "Everest Zirvesi",
            targetMeters = 8848.0,
            iconEmoji = "🏔️",
            description = "İnanılmaz! Parmağın Everest Dağı'nın zirvesinde!"
        )
    ).sortedBy { it.targetMeters }

    operator fun invoke(currentMeters: Double): MetaphorProgress {
        val safeMeters = currentMeters.coerceAtLeast(0.0)

        // Find the highest milestone already achieved
        val currentMilestone = milestones.lastOrNull { safeMeters >= it.targetMeters }
            ?: milestones.first()

        // Find the next milestone to achieve
        val nextMilestone = milestones.firstOrNull { it.targetMeters > safeMeters }

        return if (nextMilestone != null) {
            val range = nextMilestone.targetMeters - currentMilestone.targetMeters
            val progressInRange = safeMeters - currentMilestone.targetMeters
            val fraction = (progressInRange / range).toFloat().coerceIn(0f, 1f)
            val remaining = (nextMilestone.targetMeters - safeMeters).coerceAtLeast(0.0)

            MetaphorProgress(
                currentMilestone = currentMilestone,
                nextMilestone = nextMilestone,
                remainingMetersToNext = remaining,
                progressFractionToNext = fraction
            )
        } else {
            // Already conquered the final milestone
            MetaphorProgress(
                currentMilestone = currentMilestone,
                nextMilestone = null,
                remainingMetersToNext = 0.0,
                progressFractionToNext = 1.0f
            )
        }
    }
}
