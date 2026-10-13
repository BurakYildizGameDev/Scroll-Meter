package com.scrollmeter.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.scrollmeter.app.R
import com.scrollmeter.app.presentation.components.CalibrationRuler
import com.scrollmeter.app.presentation.theme.CyberCyan
import com.scrollmeter.app.presentation.theme.EmeraldContainerDark
import com.scrollmeter.app.presentation.theme.EmeraldNeonPrimary
import com.scrollmeter.app.presentation.theme.EmeraldNeonSecondary
import com.scrollmeter.app.presentation.theme.HyperViolet
import com.scrollmeter.app.presentation.theme.MidnightDark
import com.scrollmeter.app.presentation.theme.ObsidianSurface
import com.scrollmeter.app.presentation.theme.ObsidianSurfaceElevated
import com.scrollmeter.app.util.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MidnightDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_settings).uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightDark
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldNeonSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // SECTION 1: PHYSICAL CALIBRATION
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianSurface,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.screen_calibration),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 1.6.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = EmeraldNeonSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.screen_calibration_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Canvas Ruler
                            CalibrationRuler(
                                ppi = state.devicePpi,
                                multiplier = state.calibrationMultiplier
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val deltaPercent = ((state.calibrationMultiplier - 1.0f) * 100).toInt()
                            val sign = if (deltaPercent >= 0) "+$deltaPercent%" else "$deltaPercent%"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.calibration_multiplier,
                                        state.calibrationMultiplier,
                                        sign
                                    ),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldNeonPrimary
                                )
                                Text(
                                    text = stringResource(R.string.calibration_ppi, state.devicePpi.toInt()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Slider(
                                value = state.calibrationMultiplier,
                                onValueChange = { viewModel.onIntent(SettingsIntent.UpdateCalibration(it)) },
                                valueRange = 0.80f..1.20f,
                                steps = 39, // 0.01 increments
                                colors = SliderDefaults.colors(
                                    thumbColor = EmeraldNeonSecondary,
                                    activeTrackColor = EmeraldNeonPrimary,
                                    inactiveTrackColor = EmeraldContainerDark
                                )
                            )

                            OutlinedButton(
                                onClick = { viewModel.onIntent(SettingsIntent.ResetCalibration) },
                                modifier = Modifier.align(Alignment.End),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = EmeraldNeonSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.reset_to_default),
                                    color = EmeraldNeonSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                // SECTION 2: DAILY TARGET
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianSurface,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.daily_scroll_target),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 1.6.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = EmeraldNeonSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.current_goal_meters, state.dailyGoalMeters.toInt()),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick preset chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(50f, 100f, 250f, 500f).forEach { goal ->
                                    val isSelected = state.dailyGoalMeters.toInt() == goal.toInt()
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) EmeraldNeonPrimary else EmeraldContainerDark)
                                            .clickable { viewModel.onIntent(SettingsIntent.UpdateDailyGoal(goal)) }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${goal.toInt()}m",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isSelected) Color.Black else Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Slider(
                                value = state.dailyGoalMeters,
                                onValueChange = { viewModel.onIntent(SettingsIntent.UpdateDailyGoal(it)) },
                                valueRange = 25f..1000f,
                                steps = 38,
                                colors = SliderDefaults.colors(
                                    thumbColor = EmeraldNeonSecondary,
                                    activeTrackColor = EmeraldNeonPrimary,
                                    inactiveTrackColor = EmeraldContainerDark
                                )
                            )
                        }
                    }
                }

                // SECTION 2.5: TRACKING ENGINE & CADENCE
                item {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        viewModel.checkPermissions(context)
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianSurface,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.tracking_engine_title),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 1.6.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = EmeraldNeonSecondary
                            )

                            // Engine 1: Safe Mode
                            val isSafeSelected = state.trackingMode == com.scrollmeter.app.domain.model.TrackingMode.SAFE_USAGE_STATS
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onIntent(
                                            SettingsIntent.UpdateTrackingMode(com.scrollmeter.app.domain.model.TrackingMode.SAFE_USAGE_STATS)
                                        )
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSafeSelected) EmeraldContainerDark.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.03f),
                                border = if (isSafeSelected) androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldNeonPrimary) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = stringResource(R.string.badge_safe_mode),
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "ÖNERİLEN",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = EmeraldNeonPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Android Kullanım Erişimi + Doomscroll Kadans Motoru. %0 pil, sıfır kısıtlı ayar uyarısı.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSafeSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = EmeraldNeonPrimary,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }

                            // Engine 2: Lab Mode (Accessibility)
                            val isLabSelected = state.trackingMode == com.scrollmeter.app.domain.model.TrackingMode.ACCESSIBILITY_LAB
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.onIntent(
                                            SettingsIntent.UpdateTrackingMode(com.scrollmeter.app.domain.model.TrackingMode.ACCESSIBILITY_LAB)
                                        )
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isLabSelected) HyperViolet.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.03f),
                                border = if (isLabSelected) androidx.compose.foundation.BorderStroke(1.dp, HyperViolet) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(R.string.badge_lab_mode),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Erişilebilirlik Piksel Sensörü. Canlı donanım dokunma deltaları.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isLabSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = HyperViolet,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }

                            // Cadence Speed adjustment (visible if in Safe Mode)
                            if (isSafeSelected) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stringResource(R.string.cadence_speed_title),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = EmeraldNeonSecondary
                                )
                                Text(
                                    text = stringResource(R.string.cadence_speed_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        Pair(0.80f, stringResource(R.string.speed_relaxed)),
                                        Pair(1.00f, stringResource(R.string.speed_balanced)),
                                        Pair(1.25f, stringResource(R.string.speed_rapid))
                                    ).forEach { (speed, label) ->
                                        val isChosen = Math.abs(state.cadenceSpeedMultiplier - speed) < 0.05f
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isChosen) EmeraldNeonPrimary else EmeraldContainerDark)
                                                .clickable {
                                                    viewModel.onIntent(SettingsIntent.UpdateCadenceSpeed(speed))
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isChosen) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = { viewModel.onIntent(SettingsIntent.ResetDailyTelemetry) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = EmeraldNeonSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Bugünkü Telemetriyi Sıfırla (0m Başlat)",
                                    color = EmeraldNeonSecondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                // SECTION 2.8: APPLICATION LANGUAGE SELECTOR
                item {
                    var showLanguageDialog by remember { mutableStateOf(false) }
                    val currentOption = remember(state.selectedLanguage) {
                        LocaleHelper.SUPPORTED_LANGUAGES.find { it.code == state.selectedLanguage }
                            ?: LocaleHelper.SUPPORTED_LANGUAGES.first()
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianSurface,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.settings_language_title),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 1.6.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = CyberCyan
                            )
                            Text(
                                text = stringResource(R.string.settings_language_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { showLanguageDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White.copy(alpha = 0.04f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = currentOption.flagEmoji,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = currentOption.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CyberCyan.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = stringResource(R.string.btn_change_language),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CyberCyan,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (showLanguageDialog) {
                        val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
                        AlertDialog(
                            onDismissRequest = { showLanguageDialog = false },
                            title = {
                                Text(
                                    text = stringResource(R.string.settings_language_title),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            },
                            text = {
                                Column(
                                    modifier = Modifier.verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    LocaleHelper.SUPPORTED_LANGUAGES.forEach { lang ->
                                        val isSelected = state.selectedLanguage == lang.code
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable {
                                                    viewModel.onIntent(SettingsIntent.UpdateLanguage(lang.code))
                                                    showLanguageDialog = false
                                                    activity?.recreate()
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f),
                                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, CyberCyan) else null
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                                            ) {
                                                Text(text = lang.flagEmoji, fontSize = 20.sp)
                                                Text(
                                                    text = lang.displayName,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    ),
                                                    color = if (isSelected) CyberCyan else Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showLanguageDialog = false }) {
                                    Text(text = "Kapat", color = CyberCyan)
                                }
                            },
                            containerColor = ObsidianSurfaceElevated
                        )
                    }
                }

                // SECTION 3: PRIVACY GUARANTEE & ABOUT
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = ObsidianSurface,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = EmeraldNeonSecondary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.enterprise_privacy),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        letterSpacing = 1.4.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = EmeraldNeonSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${stringResource(R.string.privacy_bullet_offline)}\n" +
                                        "${stringResource(R.string.privacy_bullet_window)}\n" +
                                        "${stringResource(R.string.privacy_bullet_db)}\n" +
                                        "${stringResource(R.string.privacy_bullet_version)}",
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
