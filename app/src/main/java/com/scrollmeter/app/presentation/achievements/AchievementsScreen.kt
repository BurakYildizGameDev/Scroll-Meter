package com.scrollmeter.app.presentation.achievements

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrollmeter.app.domain.model.Achievement
import com.scrollmeter.app.domain.model.AchievementCategory
import com.scrollmeter.app.presentation.theme.ChampagneGold
import com.scrollmeter.app.presentation.theme.CyberCyan
import com.scrollmeter.app.presentation.theme.ElectricMint
import com.scrollmeter.app.presentation.theme.EmeraldContainerDark
import com.scrollmeter.app.presentation.theme.MidnightDark
import com.scrollmeter.app.presentation.theme.ObsidianSurface
import com.scrollmeter.app.presentation.theme.ObsidianSurfaceElevated

import androidx.compose.ui.res.stringResource
import com.scrollmeter.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AchievementsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MidnightDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_achievements).uppercase(),
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
                actions = {
                    IconButton(onClick = { viewModel.onIntent(AchievementsIntent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.btn_refresh),
                            tint = ElectricMint
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
                CircularProgressIndicator(color = ElectricMint)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hero Trophy Level Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = ObsidianSurface,
                        tonalElevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.trophy_room),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            letterSpacing = 1.8.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = CyberCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(
                                            R.string.trophy_unlocked_count,
                                            state.unlockedCount,
                                            state.totalCount
                                        ),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(ObsidianSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🏆", fontSize = 24.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val completionFraction = if (state.totalCount > 0) {
                                state.unlockedCount.toFloat() / state.totalCount.toFloat()
                            } else {
                                0f
                            }
                            val animatedFraction by animateFloatAsState(
                                targetValue = completionFraction,
                                label = "TrophyProgress"
                            )

                            LinearProgressIndicator(
                                progress = { animatedFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ElectricMint,
                                trackColor = EmeraldContainerDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = stringResource(
                                    R.string.trophy_total_mastery,
                                    (completionFraction * 100).toInt()
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Category Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryChip(
                            title = stringResource(R.string.filter_all),
                            isSelected = state.selectedCategory == null,
                            onClick = { viewModel.onIntent(AchievementsIntent.FilterCategory(null)) }
                        )
                        CategoryChip(
                            title = stringResource(R.string.filter_milestone),
                            isSelected = state.selectedCategory == AchievementCategory.MILESTONE,
                            onClick = { viewModel.onIntent(AchievementsIntent.FilterCategory(AchievementCategory.MILESTONE)) }
                        )
                        CategoryChip(
                            title = "Uzmanlık",
                            isSelected = state.selectedCategory == AchievementCategory.SPECIALIST,
                            onClick = { viewModel.onIntent(AchievementsIntent.FilterCategory(AchievementCategory.SPECIALIST)) }
                        )
                        CategoryChip(
                            title = stringResource(R.string.filter_habit),
                            isSelected = state.selectedCategory == AchievementCategory.HABIT,
                            onClick = { viewModel.onIntent(AchievementsIntent.FilterCategory(AchievementCategory.HABIT)) }
                        )
                        CategoryChip(
                            title = "Hassasiyet",
                            isSelected = state.selectedCategory == AchievementCategory.PRECISION,
                            onClick = { viewModel.onIntent(AchievementsIntent.FilterCategory(AchievementCategory.PRECISION)) }
                        )
                    }
                }

                // Achievement Bento Cards
                items(
                    items = state.achievements,
                    key = { it.id }
                ) { achievement ->
                    AchievementItemCard(achievement = achievement)
                }
            }
        }
    }
}

@Composable
private fun AchievementItemCard(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    val borderColor = if (achievement.isUnlocked) ElectricMint.copy(alpha = 0.5f) else Color.Transparent

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = if (achievement.isUnlocked) ObsidianSurfaceElevated else ObsidianSurface,
        tonalElevation = if (achievement.isUnlocked) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji or Lock badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (achievement.isUnlocked) EmeraldContainerDark else ObsidianSurfaceElevated.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = achievement.iconEmoji,
                    fontSize = 26.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val title = localizedAchievementTitle(achievement.id, achievement.title)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    if (achievement.isUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricMint.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.badge_unlocked),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                ),
                                color = ElectricMint
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                val description = localizedAchievementDescription(achievement.id, achievement.description)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (!achievement.isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { achievement.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyberCyan,
                        trackColor = EmeraldContainerDark.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = achievement.progressText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ElectricMint else ObsidianSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
            ),
            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun localizedAchievementTitle(id: String, fallback: String): String {
    val resId = when (id) {
        "FIRST_STEP" -> R.string.ach_first_step_title
        "GIRAFFE_CLUB" -> R.string.ach_giraffe_title
        "STATUE_OF_LIBERTY" -> R.string.ach_liberty_title
        "EIFFEL_CLIMBER" -> R.string.ach_eiffel_title
        "BURJ_KHALIFA" -> R.string.ach_burj_title
        "MARATHON_RUNNER" -> R.string.ach_marathon_title
        "SPEED_DEMON" -> R.string.ach_speed_title
        "SOCIAL_EXPLORER" -> R.string.ach_social_title
        "PRECISION_TUNER" -> R.string.ach_precision_title
        else -> null
    }
    return if (resId != null) stringResource(resId) else fallback
}

@Composable
private fun localizedAchievementDescription(id: String, fallback: String): String {
    val resId = when (id) {
        "FIRST_STEP" -> R.string.ach_first_step_desc
        "GIRAFFE_CLUB" -> R.string.ach_giraffe_desc
        "STATUE_OF_LIBERTY" -> R.string.ach_liberty_desc
        "EIFFEL_CLIMBER" -> R.string.ach_eiffel_desc
        "BURJ_KHALIFA" -> R.string.ach_burj_desc
        "MARATHON_RUNNER" -> R.string.ach_marathon_desc
        "SPEED_DEMON" -> R.string.ach_speed_desc
        "SOCIAL_EXPLORER" -> R.string.ach_social_desc
        "PRECISION_TUNER" -> R.string.ach_precision_desc
        else -> null
    }
    return if (resId != null) stringResource(resId) else fallback
}

