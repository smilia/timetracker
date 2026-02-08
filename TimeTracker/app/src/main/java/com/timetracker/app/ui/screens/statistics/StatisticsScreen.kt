package com.timetracker.app.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.hilt.navigation.compose.hiltViewModel
import com.timetracker.app.data.model.TimeNature
import com.timetracker.app.ui.theme.iOSBlue
import com.timetracker.app.ui.theme.iOSGray3
import com.timetracker.app.ui.theme.iOSGray5
import com.timetracker.app.ui.theme.iOSGray6
import com.timetracker.app.ui.theme.iOSGreen
import com.timetracker.app.ui.theme.iOSLabel
import com.timetracker.app.ui.theme.iOSOrange
import com.timetracker.app.ui.theme.iOSRed
import com.timetracker.app.ui.theme.iOSSecondaryBackground
import com.timetracker.app.ui.theme.iOSSecondaryLabel
import com.timetracker.app.ui.theme.iOSSeparator
import com.timetracker.app.ui.theme.iOSSystemBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val selectedRange by viewModel.selectedRange.collectAsState()
    val colorStats by viewModel.colorStats.collectAsState()
    val dailyStats by viewModel.dailyStats.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val averageDailyTime by viewModel.averageDailyTime.collectAsState()
    val productiveStats by viewModel.productiveStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "统计分析",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = iOSLabel
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = iOSSystemBackground,
                    titleContentColor = iOSLabel
                )
            )
        },
        containerColor = iOSSystemBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Date range selector - iOS style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iOSGray6)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val ranges = listOf(
                    StatisticsViewModel.DateRange.WEEK to "本周",
                    StatisticsViewModel.DateRange.MONTH to "本月",
                    StatisticsViewModel.DateRange.YEAR to "本年"
                )
                ranges.forEachIndexed { index, (range, label) ->
                    val isSelected = selectedRange == range
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) iOSSystemBackground else Color.Transparent)
                            .clickable { viewModel.setDateRange(range) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isSelected) iOSBlue else iOSSecondaryLabel
                        )
                    }
                }
            }

            // Summary cards - iOS style
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                iOSStatCard(
                    title = "总时长",
                    value = formatDuration(totalTime),
                    color = iOSBlue,
                    modifier = Modifier.weight(1f)
                )
                iOSStatCard(
                    title = "日均",
                    value = formatDuration(averageDailyTime),
                    color = iOSGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Productive vs Unproductive stats - iOS style
            iOSProductiveStatsCard(
                productiveMinutes = productiveStats.productiveMinutes,
                unproductiveMinutes = productiveStats.unproductiveMinutes,
                productivePercentage = productiveStats.productivePercentage,
                dominantNature = productiveStats.dominantNature,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category distribution - iOS style
            Text(
                text = "时间分配",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = iOSLabel,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Simple bar chart for categories - iOS style
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = iOSSecondaryBackground
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    colorStats.forEachIndexed { index, stat ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(stat.color))
                        } catch (e: Exception) {
                            iOSBlue
                        }
                        iOSCategoryBar(
                            categoryName = stat.colorName,
                            color = color,
                            minutes = stat.totalMinutes,
                            percentage = stat.percentage,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (index < colorStats.size - 1) {
                            Divider(
                                color = iOSSeparator.copy(alpha = 0.5f),
                                thickness = 0.5f.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily trend - iOS style
            Text(
                text = "每日趋势",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = iOSLabel,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Daily bars - iOS style
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = iOSSecondaryBackground
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    dailyStats.forEachIndexed { index, stat ->
                        iOSDailyBar(
                            date = stat.date,
                            totalMinutes = stat.totalMinutes,
                            maxMinutes = dailyStats.maxOfOrNull { it.totalMinutes } ?: 1,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                        if (index < dailyStats.size - 1) {
                            Divider(
                                color = iOSSeparator.copy(alpha = 0.5f),
                                thickness = 0.5f.dp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun iOSStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = iOSSecondaryLabel
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = color
            )
        }
    }
}

@Composable
private fun iOSProductiveStatsCard(
    productiveMinutes: Long,
    unproductiveMinutes: Long,
    productivePercentage: Float,
    dominantNature: TimeNature,
    modifier: Modifier = Modifier
) {
    val totalMinutes = productiveMinutes + unproductiveMinutes
    val productiveRatio = if (totalMinutes > 0) productiveMinutes.toFloat() / totalMinutes else 0f
    val unproductiveRatio = if (totalMinutes > 0) unproductiveMinutes.toFloat() / totalMinutes else 0f

    // 根据主导时间性质动态显示标题
    val title = when (dominantNature) {
        TimeNature.PRODUCTIVE -> "元气满满"
        TimeNature.UNPRODUCTIVE -> "摸鱼时光"
        else -> "效率分析"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = iOSSecondaryBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = iOSLabel
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iOSGray5)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (productiveRatio > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(productiveRatio.coerceAtLeast(0.01f))
                                .background(iOSGreen)
                        )
                    }
                    if (unproductiveRatio > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(unproductiveRatio.coerceAtLeast(0.01f))
                                .background(iOSOrange)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(iOSGreen)
                        )
                        Text(
                            text = "元气满满",
                            style = MaterialTheme.typography.bodySmall,
                            color = iOSSecondaryLabel
                        )
                    }
                    Text(
                        text = formatDuration(productiveMinutes),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = iOSGreen
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(iOSOrange)
                        )
                        Text(
                            text = "摸鱼时光",
                            style = MaterialTheme.typography.bodySmall,
                            color = iOSSecondaryLabel
                        )
                    }
                    Text(
                        text = formatDuration(unproductiveMinutes),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = iOSOrange
                    )
                }
            }
        }
    }
}

@Composable
private fun iOSCategoryBar(
    categoryName: String,
    color: Color,
    minutes: Long,
    percentage: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
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
                        .size(10.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = iOSLabel
                )
            }
            Text(
                text = "${percentage.toInt()}%",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = iOSSecondaryLabel
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(iOSGray5)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage / 100f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = formatDuration(minutes),
            style = MaterialTheme.typography.bodySmall,
            color = iOSSecondaryLabel
        )
    }
}

@Composable
private fun iOSDailyBar(
    date: LocalDate,
    totalMinutes: Long,
    maxMinutes: Long,
    modifier: Modifier = Modifier
) {
    val ratio = if (maxMinutes > 0) totalMinutes.toFloat() / maxMinutes else 0f
    val dateFormatter = DateTimeFormatter.ofPattern("M/d")

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = date.format(dateFormatter),
            style = MaterialTheme.typography.bodySmall,
            color = iOSSecondaryLabel,
            modifier = Modifier.width(50.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(iOSGray5)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(ratio.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(iOSBlue)
            )
        }

        Text(
            text = formatDuration(totalMinutes),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = iOSLabel,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End
        )
    }
}

private fun formatDuration(minutes: Long): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        "${hours}小时${mins}分钟"
    } else {
        "${mins}分钟"
    }
}
