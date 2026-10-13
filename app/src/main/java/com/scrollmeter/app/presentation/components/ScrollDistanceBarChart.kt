package com.scrollmeter.app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrollmeter.app.presentation.analytics.AppScrollItemUiModel
import com.scrollmeter.app.presentation.theme.EmeraldContainerDark
import com.scrollmeter.app.presentation.theme.EmeraldNeonPrimary
import com.scrollmeter.app.presentation.theme.EmeraldNeonSecondary
import com.scrollmeter.app.presentation.theme.ObsidianSurface

/**
 * High-performance hardware-accelerated bar chart for top 5 apps.
 * Implements Cyber Luxury & Mineral Glow aesthetic without 3rd party chart libraries.
 */
@Composable
fun ScrollDistanceBarChart(
    items: List<AppScrollItemUiModel>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = ObsidianSurface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOP DOMINANT APPS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = EmeraldNeonSecondary
                )
                Text(
                    text = "Top 5 Share",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No telemetry recorded for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val maxDistance = items.maxOfOrNull { it.distanceMeters }?.coerceAtLeast(1.0) ?: 1.0

                val animatedProgress by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
                    label = "BarChartAnimation"
                )

                // Canvas Area for Bars & Gridlines
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val barCount = items.take(5).size
                        val spacing = canvasWidth / (barCount * 2f + 1f)
                        val barWidth = spacing

                        // Background grid lines (25%, 50%, 75%, 100%)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        val gridLevels = listOf(0.25f, 0.5f, 0.75f, 1.0f)
                        for (level in gridLevels) {
                            val y = canvasHeight * (1f - level)
                            drawLine(
                                color = EmeraldContainerDark.copy(alpha = 0.35f),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.5f,
                                pathEffect = dashEffect
                            )
                        }

                        // Draw Bars
                        items.take(5).forEachIndexed { index, app ->
                            val normalizedHeight = (app.distanceMeters / maxDistance).toFloat()
                            val barHeight = canvasHeight * normalizedHeight * animatedProgress
                            val x = spacing + index * (barWidth + spacing)
                            val y = canvasHeight - barHeight

                            val gradientBrush = Brush.verticalGradient(
                                colors = listOf(
                                    EmeraldNeonSecondary,
                                    EmeraldNeonPrimary.copy(alpha = 0.85f),
                                    EmeraldContainerDark
                                ),
                                startY = y,
                                endY = canvasHeight
                            )

                            drawRoundRect(
                                brush = gradientBrush,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Labels (App Names & Percentage)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    items.take(5).forEach { app ->
                        Column(
                            modifier = Modifier.width(52.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${app.percentage.toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldNeonSecondary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
