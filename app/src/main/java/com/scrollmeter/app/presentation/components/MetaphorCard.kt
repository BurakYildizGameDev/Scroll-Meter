package com.scrollmeter.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrollmeter.app.domain.model.MetaphorMilestone
import com.scrollmeter.app.presentation.theme.ChampagneGold
import com.scrollmeter.app.presentation.theme.ElectricMint
import java.util.Locale

/**
 * Gamification Metaphor Card showing current unlocked milestone
 * and motivating countdown to next conquerable real-world monument.
 */
@Composable
fun MetaphorCard(
    currentMilestone: MetaphorMilestone?,
    nextMilestone: MetaphorMilestone?,
    remainingMetersToNext: Double,
    modifier: Modifier = Modifier
) {
    GlowingGlassCard(
        modifier = modifier.fillMaxWidth(),
        glowColor = ChampagneGold,
        glowAlpha = 0.15f
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Badge & Current Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberStatusBadge(
                    text = "Gamified Landmark 🏆",
                    accentColor = ChampagneGold
                )

                if (nextMilestone != null) {
                    Text(
                        text = String.format(Locale.US, "%.1f m kaldı", remainingMetersToNext),
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricMint
                    )
                }
            }

            // Milestone Hero Section
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentMilestone?.iconEmoji ?: "🌱",
                    fontSize = 40.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )

                Column {
                    Text(
                        text = currentMilestone?.name ?: "Yeni Başlangıç",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = currentMilestone?.description ?: "Parmağın henüz ısınıyor, kaydırmaya başla!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            // Next Milestone Countdown Bar
            if (nextMilestone != null) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sonraki: ${nextMilestone.iconEmoji} ${nextMilestone.name}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${nextMilestone.targetMeters.toInt()} m",
                        style = MaterialTheme.typography.labelSmall,
                        color = ChampagneGold
                    )
                }

                val currentTarget = currentMilestone?.targetMeters ?: 0.0
                val nextTarget = nextMilestone.targetMeters
                val totalSpan = (nextTarget - currentTarget).coerceAtLeast(1.0)
                val covered = (nextTarget - remainingMetersToNext - currentTarget).coerceIn(0.0, totalSpan)
                val fraction = (covered / totalSpan).toFloat()

                val animatedFraction by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = tween(durationMillis = 800),
                    label = "MetaphorFractionAnim"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedFraction)
                            .height(8.dp)
                            .background(ChampagneGold)
                    )
                }
            }
        }
    }
}
