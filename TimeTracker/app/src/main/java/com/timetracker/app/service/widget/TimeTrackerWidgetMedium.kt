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

class TimeTrackerWidgetMedium : GlanceAppWidget() {

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

        val totalMinutes = timeBlocks.sumOf {
            val startMillis = it.startTime
            val endMillis = it.endTime
            ((endMillis - startMillis) / 60000).toInt()
        }
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        provideContent {
            MediumWidgetContent(
                date = today,
                currentBlock = currentBlock?.toModel(),
                nextBlock = nextBlock?.toModel(),
                totalHours = hours,
                totalMinutes = minutes,
                blockCount = timeBlocks.size
            )
        }
    }

    @Composable
    private fun MediumWidgetContent(
        date: LocalDate,
        currentBlock: TimeBlock?,
        nextBlock: TimeBlock?,
        totalHours: Int,
        totalMinutes: Int,
        blockCount: Int
    ) {
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.Top
        ) {
            // Header
            Row(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("M月d日")),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF333333)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = "${totalHours}h${totalMinutes}m",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF4CAF50)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Current Activity
            if (currentBlock != null) {
                val color = parseColor(currentBlock.color)

                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(color.copy(alpha = 0.15f))
                        .cornerRadius(8.dp)
                        .padding(10.dp)
                ) {
                    Row {
                        Text(
                            text = "当前 · ${currentBlock.timeNature.toDisplayName()}",
                            style = TextStyle(
                                color = ColorProvider(color),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Text(
                            text = "${currentBlock.startTime.format(timeFormatter)}-${currentBlock.endTime.format(timeFormatter)}",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF666666)),
                                fontSize = 12.sp
                            )
                        )
                    }
                    Text(
                        text = currentBlock.title,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF333333)),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                }
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

            // Next Activity
            if (nextBlock != null) {
                val color = parseColor(nextBlock.color)
                val minutesUntil = java.time.Duration.between(LocalDateTime.now(), nextBlock.startTime).toMinutes()

                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5))
                        .cornerRadius(8.dp)
                        .padding(8.dp)
                ) {
                    Text(
                        text = nextBlock.title,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF333333)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1,
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        text = "${nextBlock.startTime.format(timeFormatter)} (${minutesUntil}分钟后)",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF666666)),
                            fontSize = 11.sp
                        )
                    )
                }
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
