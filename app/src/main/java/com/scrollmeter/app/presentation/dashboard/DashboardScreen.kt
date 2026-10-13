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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollmeter.app.R
import com.scrollmeter.app.presentation.components.AppIcon
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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.processIntent(DashboardIntent.Refresh)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        if (uiState.isLoading && uiState.todayMeters == 0.0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = ElectricMint,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // Top Brand & Navigation Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCROLL-METER",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.8.sp
                        ),
                        color = Color.White
                    )

                    // Clean Compact Vector Icon Navigation
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.processIntent(DashboardIntent.Refresh) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sync,
                                contentDescription = stringResource(R.string.btn_refresh),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onNavigateToAchievements,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = stringResource(R.string.nav_achievements),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onNavigateToAnalytics,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Leaderboard,
                                contentDescription = stringResource(R.string.nav_analytics),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = stringResource(R.string.nav_settings),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Telemetry Title & Status Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Günlük Telemetri",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )

                    when {
                        uiState.isDemoMode -> {
                            CyberStatusBadge(
                                text = "DEMO MODU",
                                accentColor = CyberCyan
                            )
                        }
                        uiState.trackingMode == com.scrollmeter.app.domain.model.TrackingMode.SAFE_USAGE_STATS -> {
                            CyberStatusBadge(
                                text = "STANDART MOD",
                                accentColor = ElectricMint
                            )
                        }
                        else -> {
                            CyberStatusBadge(
                                text = "LAB SENSÖRÜ",
                                accentColor = HyperViolet
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Hero Circular Gauge (Adaptive Scale: cm when <1m, m when <1km, km when >=1km)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val meterUnit = stringResource(R.string.unit_meter)
                    val (formattedDistance, unit) = remember(uiState.todayMeters, meterUnit) {
                        when {
                            uiState.todayMeters >= 1000.0 -> {
                                String.format(Locale.US, "%.2f", uiState.todayMeters / 1000.0) to "km"
                            }
                            uiState.todayMeters >= 1.0 -> {
                                String.format(Locale.US, "%.2f", uiState.todayMeters) to meterUnit
                            }
                            else -> {
                                val cm = (uiState.todayMeters * 100.0).coerceAtLeast(0.0)
                                String.format(Locale.US, "%.1f", cm) to "cm"
                            }
                        }
                    }
                    CircularDistanceProgress(
                        progressFraction = uiState.progressPercentage,
                        distanceValue = formattedDistance,
                        unitText = unit,
                        subtitle = stringResource(R.string.today_distance)
                    )
                }

                // Bento Metrics Row (Dominant App + Goal Pacing)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Tile 1: Dominant App
                    GlowingGlassCard(
                        modifier = Modifier.weight(1.1f),
                        onClick = onNavigateToAnalytics
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.leader_app),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.0.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val dominantName = uiState.dominantApp?.appDisplayName ?: stringResource(R.string.no_dominant_app)
                            val dominantDistance = uiState.dominantApp?.physicalDistance?.formattedAdaptive ?: "0.0 cm"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.dominantApp?.let { app ->
                                    AppIcon(
                                        packageName = app.packageName,
                                        size = 32.dp
                                    )
                                }
                                Column {
                                    Text(
                                        text = dominantName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = dominantDistance,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ElectricMint
                                    )
                                }
                            }
                        }
                    }

                    // Tile 2: Goal Pacing
                    GlowingGlassCard(
                        modifier = Modifier.weight(0.9f),
                        onClick = onNavigateToSettings
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.daily_goal),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.0.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${uiState.dailyGoalMeters.toInt()} m",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val percent = (uiState.progressPercentage * 100).toInt()
                            Text(
                                text = stringResource(R.string.goal_completed_pct, percent),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Landmark Milestone Card
                MetaphorCard(
                    currentMilestone = uiState.currentMetaphor,
                    nextMilestone = uiState.nextMetaphor,
                    remainingMetersToNext = uiState.metersRemainingToNext
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
