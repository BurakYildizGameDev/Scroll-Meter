package com.scrollmeter.app.presentation.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.scrollmeter.app.presentation.theme.CyberCyan
import com.scrollmeter.app.presentation.theme.ElectricMint

/**
 * Custom Canvas Circular Distance Gauge featuring an open arc with
 * glowing neon sweep gradients, spring animations, and centered metric display.
 */
@Composable
fun CircularDistanceProgress(
    progressFraction: Float,
    distanceValue: String,
    modifier: Modifier = Modifier,
    unitText: String = "m",
    subtitle: String = "GÜNLÜK HEDEF",
    sizeDp: Dp = 220.dp,
    strokeWidth: Dp = 16.dp,
    trackColor: Color = Color.White.copy(alpha = 0.08f),
    gradientColors: List<Color> = listOf(ElectricMint, CyberCyan, ElectricMint)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
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

            val startAngle = 140f
            val maxSweepAngle = 260f

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

            // Animated Foreground Glowing Arc
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

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = distanceValue,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Text(
                    text = " $unitText",
                    style = MaterialTheme.typography.titleLarge,
                    color = ElectricMint,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            val percentageInt = (progressFraction * 100).toInt()
            Text(
                text = "%$percentageInt Tamamlandı",
                style = MaterialTheme.typography.labelMedium,
                color = ElectricMint
            )
        }
    }
}
