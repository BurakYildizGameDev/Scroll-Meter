package com.scrollmeter.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.scrollmeter.app.presentation.theme.GlassBorderGradient
import com.scrollmeter.app.presentation.theme.ObsidianSurface
import com.scrollmeter.app.presentation.theme.ObsidianSurfaceGlass

/**
 * Premium Frosted Glass Card featuring subtle outer neon glow,
 * hairline refractive border, and tactile corner radii.
 */
@Composable
fun GlowingGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(26.dp),
    glowColor: Color = Color.Transparent,
    glowAlpha: Float = 0.12f,
    glowStrokeWidth: Dp = 10.dp,
    borderBrush: Brush = GlassBorderGradient,
    backgroundColor: Color = ObsidianSurface.copy(alpha = 0.72f),
    contentPadding: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            // 1. Outer ambient light glow (drawn behind the card without clipping)
            .drawBehind {
                if (glowColor != Color.Transparent) {
                    val cornerPx = shape.topStart.toPx(size, this)
                    drawRoundRect(
                        color = glowColor.copy(alpha = glowAlpha),
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx),
                        style = Stroke(width = glowStrokeWidth.toPx())
                    )
                }
            }
            .then(clickableModifier)
            // 2. Frosted glass clipping & styling
            .clip(shape)
            .background(backgroundColor)
            // 3. Hairline metallic refraction border
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .padding(contentPadding),
        content = content
    )
}
