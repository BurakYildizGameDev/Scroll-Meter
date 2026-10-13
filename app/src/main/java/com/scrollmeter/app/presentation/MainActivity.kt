package com.scrollmeter.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.scrollmeter.app.presentation.components.CyberStatusBadge
import com.scrollmeter.app.presentation.components.GlowingGlassCard
import com.scrollmeter.app.presentation.theme.CyberCyan
import com.scrollmeter.app.presentation.theme.ElectricMint
import com.scrollmeter.app.presentation.theme.HyperViolet
import com.scrollmeter.app.presentation.theme.ObsidianVoid
import com.scrollmeter.app.presentation.theme.ScrollMeterTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScrollMeterTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianVoid
                ) {
                    PremiumGenZDashboardPreview()
                }
            }
        }
    }
}

@Composable
fun PremiumGenZDashboardPreview() {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Ambient Cyber Glow on top of the viewport
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricMint.copy(alpha = 0.14f),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCROLL-METER",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Finger Odyssey",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }

                CyberStatusBadge(
                    text = "Live Sync ⚡",
                    accentColor = ElectricMint
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hero Bento Card (Main Metric)
            GlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = ElectricMint,
                glowAlpha = 0.20f
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TODAY'S DISTANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricMint
                        )
                        Text(
                            text = "Top 5% Finger Marathon 🏃",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "142.50",
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White
                        )
                        Text(
                            text = " m",
                            style = MaterialTheme.typography.headlineMedium,
                            color = ElectricMint,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🔥 You cooked today. Unlocked: Zürafa (15m) & Eyfel Kulesi (330m) countdown active!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            // Bento Grid Row (Asymmetric 2-Column)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Bento Tile 1: Top App
                GlowingGlassCard(
                    modifier = Modifier.weight(1.1f),
                    glowColor = CyberCyan,
                    glowAlpha = 0.12f
                ) {
                    Column {
                        Text(
                            text = "DOMINANT APP",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Instagram",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "88.4 m • 62%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Bento Tile 2: Touch Grass Index
                GlowingGlassCard(
                    modifier = Modifier.weight(0.9f),
                    glowColor = HyperViolet,
                    glowAlpha = 0.15f
                ) {
                    Column {
                        Text(
                            text = "TOUCH GRASS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD6BBFB)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "82%",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Text(
                            text = "Ceiling safe 🌱",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Metaphor Progression Card (Full Width)
            GlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = Color(0xFFF5E6B8),
                glowAlpha = 0.12f
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CURRENT METAPHOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF5E6B8)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🗽 Özgürlük Heykeli",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Only 45m left to unlock Football Pitch! ⚽",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
