package com.timetracker.app.service.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.timetracker.app.MainActivity
import com.timetracker.app.data.local.database.TimeTrackerDatabase
import com.timetracker.app.data.model.TimeBlock
import com.timetracker.app.data.model.TimeNature
import com.timetracker.app.data.model.toDisplayName
import com.timetracker.app.data.model.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeTrackerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = TimeTrackerDatabase.getInstance(context)
        val timeBlockDao = database.timeBlockDao()
        val categoryDao = database.categoryDao()

        val today = LocalDate.now()
        val todayMillis = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val tomorrowMillis = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

        val timeBlocks = withContext(Dispatchers.IO) {
            timeBlockDao.getTimeBlocksForWidget(todayMillis, tomorrowMillis)
        }

        val categories = withContext(Dispatchers.IO) {
            categoryDao.getAllCategoriesForWidget()
        }.associateBy { it.id }

        val currentTime = LocalDateTime.now()
        val currentTimeMillis = java.time.ZoneId.systemDefault().let {
            currentTime.atZone(it).toInstant().toEpochMilli()
        }

        val currentBlock = timeBlocks.find { block ->
            block.startTime <= currentTimeMillis && block.endTime > currentTimeMillis
        }

        val nextBlock = timeBlocks.filter { it.startTime > currentTimeMillis }.minByOrNull { it.startTime }

        provideContent {
            WidgetContent(
                date = today,
                timeBlocks = timeBlocks.map { it.toModel() },
                currentBlock = currentBlock?.toModel(),
                nextBlock = nextBlock?.toModel()
            )
        }
    }

    @Composable
    private fun WidgetContent(
        date: LocalDate,
        timeBlocks: List<TimeBlock>,
        currentBlock: TimeBlock?,
        nextBlock: TimeBlock?
    ) {
        val totalMinutes = timeBlocks.sumOf {
            val startMillis = it.startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = it.endTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            ((endMillis - startMillis) / 60000).toInt()
        }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.Top
        ) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("M月d日")),
                style = TextStyle(
                    color = ColorProvider(Color(0xFF333333)),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )

            if (currentBlock != null) {
                val color = parseColor(currentBlock.color)
                CurrentActivityCard(
                    title = currentBlock.title,
                    categoryName = currentBlock.timeNature.toDisplayName(),
                    color = color,
                    startTime = currentBlock.startTime,
                    endTime = currentBlock.endTime
                )
            } else {
                Text(
                    text = "当前无活动",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF999999)),
                        fontSize = 14.sp
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (nextBlock != null) {
                val color = parseColor(nextBlock.color)
                NextActivityCard(
                    title = nextBlock.title,
                    categoryName = nextBlock.timeNature.toDisplayName(),
                    color = color,
                    startTime = nextBlock.startTime
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            DailySummaryCard(
                totalHours = hours,
                totalMinutes = minutes,
                blockCount = timeBlocks.size
            )

            if (timeBlocks.isNotEmpty()) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                MiniTimeline(timeBlocks = timeBlocks)
            }
        }
    }

    @Composable
    private fun CurrentActivityCard(
        title: String,
        categoryName: String,
        color: Color,
        startTime: LocalDateTime,
        endTime: LocalDateTime
    ) {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(color.copy(alpha = 0.15f))
                .cornerRadius(8.dp)
                .padding(10.dp)
        ) {
            Text(
                text = "当前活动",
                style = TextStyle(
                    color = ColorProvider(color),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(
                text = title,
                style = TextStyle(
                    color = ColorProvider(Color(0xFF333333)),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = "$categoryName · ${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF666666)),
                    fontSize = 12.sp
                )
            )
        }
    }

    @Composable
    private fun NextActivityCard(
        title: String,
        categoryName: String,
        color: Color,
        startTime: LocalDateTime
    ) {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val minutesUntil = java.time.Duration.between(LocalDateTime.now(), startTime).toMinutes()

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .cornerRadius(8.dp)
                .padding(8.dp)
        ) {
            Row {
                Text(
                    text = title,
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF333333)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )
                Text(
                    text = "${startTime.format(timeFormatter)}",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF666666)),
                        fontSize = 12.sp
                    )
                )
            }
            Text(
                text = "$categoryName · ${minutesUntil}分钟后开始",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF888888)),
                    fontSize = 11.sp
                )
            )
        }
    }

    @Composable
    private fun DailySummaryCard(
        totalHours: Int,
        totalMinutes: Int,
        blockCount: Int
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Color(0xFFE8F5E9))
                .cornerRadius(8.dp)
                .padding(10.dp)
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "今日已记录",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF666666)),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "${totalHours}小时${totalMinutes}分钟",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF4CAF50)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "活动数",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF666666)),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "$blockCount",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF4CAF50)),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }

    @Composable
    private fun MiniTimeline(
        timeBlocks: List<TimeBlock>
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .cornerRadius(8.dp)
                .padding(8.dp)
        ) {
            Text(
                text = "今日时间线 (${timeBlocks.size}个活动)",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF666666)),
                    fontSize = 11.sp
                ),
                modifier = GlanceModifier.padding(bottom = 4.dp)
            )

            Row(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Text(
                    text = "06:00",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF999999)),
                        fontSize = 9.sp
                    ),
                    modifier = GlanceModifier.padding(end = 8.dp)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "14:00",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF999999)),
                        fontSize = 9.sp
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "23:00",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF999999)),
                        fontSize = 9.sp
                    ),
                    modifier = GlanceModifier.padding(start = 8.dp)
                )
            }
        }
    }

    private fun parseColor(colorString: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (e: Exception) {
            Color(0xFF8BC34A)
        }
    }

    private fun com.timetracker.app.data.local.entity.TimeBlockEntity.toModel(): TimeBlock {
        return TimeBlock(
            id = this.id,
            color = this.color,
            title = this.title,
            startTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(this.startTime),
                java.time.ZoneId.systemDefault()
            ),
            endTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(this.endTime),
                java.time.ZoneId.systemDefault()
            ),
            date = LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(this.date),
                java.time.ZoneId.systemDefault()
            ),
            note = this.note,
            timeNature = try {
                TimeNature.valueOf(this.timeNature)
            } catch (e: Exception) {
                TimeNature.PRODUCTIVE
            }
        )
    }
}
