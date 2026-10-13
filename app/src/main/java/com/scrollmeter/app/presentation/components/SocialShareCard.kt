package com.scrollmeter.app.presentation.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.scrollmeter.app.R
import com.scrollmeter.app.presentation.theme.ChampagneGold
import com.scrollmeter.app.presentation.theme.CyberCyan
import com.scrollmeter.app.presentation.theme.ElectricMint
import com.scrollmeter.app.presentation.theme.HyperViolet
import com.scrollmeter.app.presentation.theme.ObsidianSurface
import com.scrollmeter.app.presentation.theme.ObsidianSurfaceElevated
import java.util.Locale

enum class ScrollTier(
    val tierIndex: Int,
    val iconEmoji: String,
    val accentColor: Color,
    val titleRes: Int,
    val badgeRes: Int,
    val tweetTemplateRes: Int
) {
    ZEN(
        tierIndex = 1,
        iconEmoji = "🧘‍♂️",
        accentColor = ElectricMint,
        titleRes = R.string.share_tier_zen_title,
        badgeRes = R.string.share_tier_zen_badge,
        tweetTemplateRes = R.string.share_tier_zen_tweet
    ),
    EXPLORER(
        tierIndex = 2,
        iconEmoji = "🏃",
        accentColor = CyberCyan,
        titleRes = R.string.share_tier_explorer_title,
        badgeRes = R.string.share_tier_explorer_badge,
        tweetTemplateRes = R.string.share_tier_explorer_tweet
    ),
    CLIMBER(
        tierIndex = 3,
        iconEmoji = "🗼",
        accentColor = HyperViolet,
        titleRes = R.string.share_tier_climber_title,
        badgeRes = R.string.share_tier_climber_badge,
        tweetTemplateRes = R.string.share_tier_climber_tweet
    ),
    CONQUEROR(
        tierIndex = 4,
        iconEmoji = "🏢",
        accentColor = ChampagneGold,
        titleRes = R.string.share_tier_conqueror_title,
        badgeRes = R.string.share_tier_conqueror_badge,
        tweetTemplateRes = R.string.share_tier_conqueror_tweet
    ),
    TITAN(
        tierIndex = 5,
        iconEmoji = "🚨",
        accentColor = Color(0xFFF87171),
        titleRes = R.string.share_tier_titan_title,
        badgeRes = R.string.share_tier_titan_badge,
        tweetTemplateRes = R.string.share_tier_titan_tweet
    );

    companion object {
        fun fromMeters(meters: Double): ScrollTier {
            return when {
                meters < 20.0 -> ZEN
                meters < 100.0 -> EXPLORER
                meters < 330.0 -> CLIMBER
                meters < 1000.0 -> CONQUEROR
                else -> TITAN
            }
        }
    }
}

@Composable
fun SocialShareCard(
    todayMeters: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTier = remember(todayMeters) { ScrollTier.fromMeters(todayMeters) }

    val formattedDistance = remember(todayMeters) {
        when {
            todayMeters >= 1000.0 -> String.format(Locale.US, "%.2f km", todayMeters / 1000.0)
            todayMeters >= 1.0 -> String.format(Locale.US, "%.1f m", todayMeters)
            else -> String.format(Locale.US, "%.1f cm", todayMeters * 100.0)
        }
    }

    val tweetText = stringResource(currentTier.tweetTemplateRes, formattedDistance)
    val copiedToastText = stringResource(R.string.share_copied_toast)

    val progressFraction = remember(currentTier) {
        (currentTier.tierIndex / 5.0f).coerceIn(0.2f, 1.0f)
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "tierProgress"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = ObsidianSurface,
        tonalElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    currentTier.accentColor.copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.05f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(currentTier.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentTier.iconEmoji, fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.share_card_title),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = stringResource(currentTier.titleRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = currentTier.accentColor
                        )
                    }
                }

                CyberStatusBadge(
                    text = stringResource(currentTier.badgeRes),
                    accentColor = currentTier.accentColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5-Step Animated Tier Milestone Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = currentTier.accentColor,
                    trackColor = Color.White.copy(alpha = 0.08f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ScrollTier.entries.forEach { tier ->
                        val isReached = tier.tierIndex <= currentTier.tierIndex
                        Text(
                            text = "${tier.iconEmoji} ${tier.tierIndex}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isReached) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            ),
                            color = if (isReached) currentTier.accentColor else Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Tweet Preview Bubble
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = ObsidianSurfaceElevated.copy(alpha = 0.7f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Text(
                    text = "“$tweetText”",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons (Share on X + Copy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { shareOnX(context, tweetText) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentTier.accentColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.share_on_x_btn),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Scroll-Meter Progress", tweetText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, copiedToastText, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(0.9f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.share_copy_btn),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun shareOnX(context: Context, text: String) {
    try {
        val encoded = Uri.encode(text)
        val twitterUri = Uri.parse("https://twitter.com/intent/tweet?text=$encoded")
        val browserIntent = Intent(Intent.ACTION_VIEW, twitterUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(browserIntent)
    } catch (_: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Milestone"))
    }
}
