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
import com.timetracker.app.data.model.toModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeTrackerWidgetSmall : GlanceAppWidget() {

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

        val totalMinutes = timeBlocks.sumOf {
            val startMillis = it.startTime
            val endMillis = it.endTime
            ((endMillis - startMillis) / 60000).toInt()
        }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        provideContent {
            SmallWidgetContent(
                date = today,
                currentBlock = currentBlock?.toModel(),
                totalHours = hours,
                totalMinutes = minutes,
                blockCount = timeBlocks.size
            )
        }
    }

    @Composable
    private fun SmallWidgetContent(
        date: LocalDate,
        currentBlock: TimeBlock?,
        totalHours: Int,
        totalMinutes: Int,
        blockCount: Int
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.Top
        ) {
            // Date
            Text(
                text = date.format(DateTimeFormatter.ofPattern("M月d日")),
                style = TextStyle(
                    color = ColorProvider(Color(0xFF333333)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )

            // Current Activity (compact)
            if (currentBlock != null) {
                val color = parseColor(currentBlock.color)
                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(color.copy(alpha = 0.15f))
                        .cornerRadius(8.dp)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "当前",
                        style = TextStyle(
                            color = ColorProvider(color),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = currentBlock.title,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF333333)),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "${currentBlock.startTime.format(timeFormatter)}-${currentBlock.endTime.format(timeFormatter)}",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF666666)),
                            fontSize = 11.sp
                        )
                    )
                }
            } else {
                Text(
                    text = "当前无活动",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF999999)),
                        fontSize = 12.sp
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Stats row
            Row(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StatItem("${totalHours}h${totalMinutes}m", "已记录")
                }
                Column(
                    modifier = GlanceModifier.defaultWeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StatItem("$blockCount", "活动")
                }
            }
        }
    }

    @Composable
    private fun StatItem(value: String, label: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = TextStyle(
                    color = ColorProvider(Color(0xFF4CAF50)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = label,
                style = TextStyle(
                    color = ColorProvider(Color(0xFF666666)),
                    fontSize = 10.sp
                )
            )
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
