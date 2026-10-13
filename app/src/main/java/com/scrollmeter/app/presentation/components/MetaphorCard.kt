package com.scrollmeter.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrollmeter.app.domain.model.MetaphorMilestone
import com.scrollmeter.app.presentation.theme.ChampagneGold
import com.scrollmeter.app.presentation.theme.ElectricMint
import java.util.Locale

/**
 * Clean architectural milestone card showing current milestone status
 * and telemetry countdown to next physical real-world landmark.
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
        backgroundColor = Color(0xFF111722),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Category & Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DİJİTAL MİHENK TAŞI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (nextMilestone != null) {
                    Text(
                        text = String.format(Locale.US, "%.1f m kaldı", remainingMetersToNext),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = ElectricMint
                    )
                }
            }

            // Milestone Content
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF182232))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentMilestone?.iconEmoji ?: "🌱",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = currentMilestone?.name ?: "Yeni Başlangıç",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = currentMilestone?.description ?: "Parmağın henüz ısınıyor, kaydırmaya başla!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Next Milestone Progress
            if (nextMilestone != null) {
                val currentTarget = currentMilestone?.targetMeters ?: 0.0
                val nextTarget = nextMilestone.targetMeters
                val totalSpan = (nextTarget - currentTarget).coerceAtLeast(1.0)
                val covered = (nextTarget - remainingMetersToNext - currentTarget).coerceIn(0.0, totalSpan)
                val fraction = (covered / totalSpan).toFloat()

                val animatedFraction by animateFloatAsState(
                    targetValue = fraction,
                    animationSpec = tween(durationMillis = 600),
                    label = "MetaphorFractionAnim"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sonraki Hedef: ${nextMilestone.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${nextMilestone.targetMeters.toInt()} m",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = ChampagneGold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF1A2334))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedFraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(ChampagneGold)
                        )
                    }
                }
            }
        }
    }
}
