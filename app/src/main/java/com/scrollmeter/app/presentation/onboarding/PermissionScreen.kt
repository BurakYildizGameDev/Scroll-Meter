package com.scrollmeter.app.presentation.onboarding

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.scrollmeter.app.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.scrollmeter.app.presentation.components.CyberStatusBadge
import com.scrollmeter.app.presentation.components.GlowingGlassCard
import com.scrollmeter.app.presentation.theme.CyberCyan
import com.scrollmeter.app.presentation.theme.ElectricMint
import com.scrollmeter.app.presentation.theme.HyperViolet
import com.scrollmeter.app.presentation.theme.ObsidianVoid

/**
 * Onboarding and Accessibility Permission Screen with Cyber Luxury aesthetics
 * highlighting zero-data intrusion guarantees.
 */
@Composable
fun PermissionScreen(
    onPermissionGranted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var isGranted by remember { mutableStateOf(isAccessibilityPermissionGranted(context)) }

    // Re-check permission whenever user resumes app from Settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val granted = isAccessibilityPermissionGranted(context)
                isGranted = granted
                if (granted) {
                    onPermissionGranted()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            ElectricMint.copy(alpha = 0.16f),
                            HyperViolet.copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, 0f),
                        radius = size.width * 0.9f
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
            Spacer(modifier = Modifier.height(16.dp))

            // Header Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberStatusBadge(
                    text = stringResource(R.string.welcome_odyssey),
                    accentColor = ElectricMint
                )
            }

            Text(
                text = stringResource(R.string.track_every_meter),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.75f)
            )

            // Trust Card 1: Zero Text Reading
            GlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = ElectricMint,
                glowAlpha = 0.12f
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = ElectricMint,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.trust_privacy_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.trust_privacy_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Trust Card 2: Offline & Zero Network
            GlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = CyberCyan,
                glowAlpha = 0.12f
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.trust_network_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.trust_network_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Trust Card 3: Zero Battery Leak
            GlowingGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = HyperViolet,
                glowAlpha = 0.14f
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = Color(0xFFD6BBFB),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.trust_battery_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.trust_battery_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f, fill = false))

            // Action Button
            Button(
                onClick = {
                    if (isGranted) {
                        onPermissionGranted()
                    } else {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) ElectricMint else ElectricMint,
                    contentColor = Color.Black
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGranted) stringResource(R.string.btn_continue) else stringResource(R.string.btn_activate_service),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Checks whether ScrollAccessibilityService is currently enabled in Android system settings.
 */
fun isAccessibilityPermissionGranted(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    val servicePackage = context.packageName
    return enabledServices.any { it.resolveInfo?.serviceInfo?.packageName == servicePackage }
}
