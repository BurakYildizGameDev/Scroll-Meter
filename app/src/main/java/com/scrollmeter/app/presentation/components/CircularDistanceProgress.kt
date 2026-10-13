package com.scrollmeter.app.presentation.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrollmeter.app.presentation.theme.CyberCyan
import com.scrollmeter.app.presentation.theme.ElectricMint

/**
 * Precision Telemetry Gauge with open circular arc, smooth easing,
 * and high-contrast typography hierarchy.
 */
@Composable
fun CircularDistanceProgress(
    progressFraction: Float,
    distanceValue: String,
    modifier: Modifier = Modifier,
    unitText: String = "m",
    subtitle: String = "GÜNLÜK MESAFE",
    sizeDp: Dp = 230.dp,
    strokeWidth: Dp = 14.dp,
    trackColor: Color = Color(0xFF182232),
    gradientColors: List<Color> = listOf(ElectricMint, CyberCyan, ElectricMint)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 900,
            easing = FastOutSlowInEasing
        ),
        label = "CircularProgressArc"
    )

    val sweepGradient = Brush.sweepGradient(gradientColors)

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = size.width - strokePx
            val topLeftOffset = strokePx / 2f

            val startAngle = 135f
            val maxSweepAngle = 270f

            // Background Track Arc
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = maxSweepAngle,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Animated Telemetry Arc
            if (animatedProgress > 0f) {
                drawArc(
                    brush = sweepGradient,
                    startAngle = startAngle,
                    sweepAngle = maxSweepAngle * animatedProgress,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(topLeftOffset, topLeftOffset),
                    size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Metric Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = distanceValue,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.0).sp
                    ),
                    color = Color.White
                )
                Text(
                    text = " $unitText",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = ElectricMint,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            val percentageInt = (progressFraction * 100).toInt()
            Text(
                text = "%$percentageInt Tamamlandı",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (percentageInt >= 100) ElectricMint else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
