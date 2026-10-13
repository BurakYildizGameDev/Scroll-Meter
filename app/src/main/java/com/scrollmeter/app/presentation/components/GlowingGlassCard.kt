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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.scrollmeter.app.presentation.theme.GlassBorderGradient
import com.scrollmeter.app.presentation.theme.ObsidianSurface

/**
 * Modern tactile container card with clean hairline borders,
 * refined dark slate background, and disciplined corner radius.
 */
@Composable
fun GlowingGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    glowColor: Color = Color.Transparent,
    glowAlpha: Float = 0.08f,
    glowStrokeWidth: Dp = 1.dp,
    borderBrush: Brush = GlassBorderGradient,
    backgroundColor: Color = ObsidianSurface,
    contentPadding: Dp = 18.dp,
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

    val finalBorderBrush = if (glowColor != Color.Transparent) {
        Brush.verticalGradient(
            colors = listOf(
                glowColor.copy(alpha = 0.35f),
                glowColor.copy(alpha = 0.08f)
            )
        )
    } else {
        borderBrush
    }

    Box(
        modifier = modifier
            .then(clickableModifier)
            .clip(shape)
            .background(backgroundColor)
            .border(width = 1.dp, brush = finalBorderBrush, shape = shape)
            .padding(contentPadding),
        content = content
    )
}
