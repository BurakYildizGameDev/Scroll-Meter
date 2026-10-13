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
import com.scrollmeter.app.R
import com.scrollmeter.app.presentation.components.CalibrationRuler
import com.scrollmeter.app.presentation.theme.EmeraldContainerDark
import com.scrollmeter.app.presentation.theme.EmeraldNeonPrimary
import com.scrollmeter.app.presentation.theme.EmeraldNeonSecondary
import com.scrollmeter.app.presentation.theme.MidnightDark
import com.scrollmeter.app.presentation.theme.ObsidianSurface

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
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
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
