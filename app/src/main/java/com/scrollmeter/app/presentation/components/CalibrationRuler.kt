package com.scrollmeter.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrollmeter.app.presentation.theme.EmeraldContainerDark
import com.scrollmeter.app.presentation.theme.EmeraldNeonPrimary
import com.scrollmeter.app.presentation.theme.EmeraldNeonSecondary
import com.scrollmeter.app.presentation.theme.ObsidianSurface

/**
 * On-screen physical ruler calibrated in real millimeters.
 * Allows users to place a physical ruler on screen to tune their calibration multiplier.
 */
@Composable
fun CalibrationRuler(
    ppi: Float,
    multiplier: Float,
    modifier: Modifier = Modifier,
    rulerLengthMm: Int = 80 // 8 cm ruler length fits virtually all smartphone widths
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSurface,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                // 1 inch = 25.4 mm
                // 1 mm in pixels = (ppi / 25.4) * multiplier
                val effectivePpi = (if (ppi > 50f) ppi else 400f) * multiplier
                val pixelsPerMm = effectivePpi / 25.4f

                val startX = 24f
                val baselineY = size.height - 24f

                // Draw baseline horizontal line
                val totalRulerWidthPx = pixelsPerMm * rulerLengthMm
                drawLine(
                    color = EmeraldNeonPrimary,
                    start = Offset(startX, baselineY),
                    end = Offset(startX + totalRulerWidthPx, baselineY),
                    strokeWidth = 3f
                )

                // Paint for numbers
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 30f
                    isAntiAlias = true
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                // Draw tick marks for each millimeter
                for (mm in 0..rulerLengthMm) {
                    val currentX = startX + (mm * pixelsPerMm)
                    if (currentX > size.width - 10f) break

                    val isCentimeter = (mm % 10 == 0)
                    val isHalfCentimeter = (mm % 5 == 0)

                    val tickHeight = when {
                        isCentimeter -> 44f
                        isHalfCentimeter -> 28f
                        else -> 16f
                    }

                    val tickColor = when {
                        isCentimeter -> EmeraldNeonSecondary
                        isHalfCentimeter -> EmeraldNeonPrimary
                        else -> EmeraldContainerDark
                    }

                    drawLine(
                        color = tickColor,
                        start = Offset(currentX, baselineY),
                        end = Offset(currentX, baselineY - tickHeight),
                        strokeWidth = if (isCentimeter) 3.5f else 2f
                    )

                    if (isCentimeter) {
                        val cmNumber = mm / 10
                        drawContext.canvas.nativeCanvas.drawText(
                            "$cmNumber",
                            currentX,
                            baselineY - tickHeight - 8f,
                            textPaint
                        )
                    }
                }
            }
        }
    }
}
