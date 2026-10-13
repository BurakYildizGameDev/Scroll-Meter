package com.scrollmeter.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollmeter.app.R
import com.scrollmeter.app.presentation.components.CircularDistanceProgress
import com.scrollmeter.app.presentation.components.CyberStatusBadge
import com.scrollmeter.app.presentation.components.GlowingGlassCard
import com.scrollmeter.app.presentation.components.MetaphorCard
import com.scrollmeter.app.presentation.theme.CyberCyan
import com.scrollmeter.app.presentation.theme.ElectricMint
import com.scrollmeter.app.presentation.theme.HyperViolet
import com.scrollmeter.app.presentation.theme.ObsidianVoid
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToAchievements: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricMint.copy(alpha = 0.16f),
                            HyperViolet.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, 0f),
                        radius = size.width * 0.85f
                    )
                )
            }
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        if (uiState.isLoading && uiState.todayMeters == 0.0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricMint)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "SCROLL-METER",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (uiState.isDemoMode) {
                                CyberStatusBadge(
                                    text = "DEMO / PORTFOLIO",
                                    accentColor = CyberCyan
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.finger_odyssey),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.IconButton(onClick = onNavigateToAchievements) {
                            Text(text = "🏆", fontSize = 20.sp)
                        }
                        androidx.compose.material3.IconButton(onClick = onNavigateToAnalytics) {
                            Text(text = "📊", fontSize = 18.sp)
                        }
                        androidx.compose.material3.IconButton(onClick = onNavigateToSettings) {
                            Text(text = "⚙️", fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Hero Circular Gauge
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val formattedDistance = String.format(Locale.US, "%.1f", uiState.todayMeters)
                    CircularDistanceProgress(
                        progressFraction = uiState.progressPercentage,
                        distanceValue = formattedDistance,
                        unitText = stringResource(R.string.unit_meter),
                        subtitle = stringResource(R.string.today_distance)
                    )
                }

                // Bento Row (Dominant App + Touch Grass Ceiling)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tile 1: Dominant App
                    GlowingGlassCard(
                        modifier = Modifier.weight(1.1f),
                        glowColor = CyberCyan,
                        glowAlpha = 0.14f,
                        onClick = onNavigateToAnalytics
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.leader_app),
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val dominantName = uiState.dominantApp?.appDisplayName ?: stringResource(R.string.no_dominant_app)
                            val dominantDistance = uiState.dominantApp?.physicalDistance?.formattedMeters ?: "0.00 m"
                            Text(
                                text = dominantName,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            Text(
                                text = dominantDistance,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Tile 2: Goal Pacing
                    GlowingGlassCard(
                        modifier = Modifier.weight(0.9f),
                        glowColor = HyperViolet,
                        glowAlpha = 0.15f,
                        onClick = onNavigateToSettings
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.daily_goal),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD6BBFB)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${uiState.dailyGoalMeters.toInt()} m",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.White
                            )
                            val percent = (uiState.progressPercentage * 100).toInt()
                            Text(
                                text = stringResource(R.string.goal_completed_pct, percent),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Metaphor Gamification Milestone Card (Faz 50)
                MetaphorCard(
                    currentMilestone = uiState.currentMetaphor,
                    nextMilestone = uiState.nextMetaphor,
                    remainingMetersToNext = uiState.metersRemainingToNext
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
