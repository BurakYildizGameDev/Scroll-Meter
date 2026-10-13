package com.scrollmeter.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrollmeter.app.presentation.theme.ElectricMint

/**
 * Minimalist, high-precision status badge with subtle indicator dot.
 */
@Composable
fun CyberStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = ElectricMint,
    isPulsing: Boolean = false
) {
    Row(
        modifier = modifier
            .background(
                color = accentColor.copy(alpha = 0.10f),
                shape = RoundedCornerShape(100.dp)
            )
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.25f),
                shape = RoundedCornerShape(100.dp)
            )
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(accentColor, CircleShape)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = accentColor,
            maxLines = 1,
            softWrap = false
        )
    }
}
