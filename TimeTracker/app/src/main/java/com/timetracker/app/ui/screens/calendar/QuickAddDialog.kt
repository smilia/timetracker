package com.timetracker.app.ui.screens.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timetracker.app.data.model.Template
import com.timetracker.app.data.model.TimeBlock
import com.timetracker.app.data.model.TimeNature
import com.timetracker.app.ui.theme.iOSBlue
import com.timetracker.app.ui.theme.iOSGray3
import com.timetracker.app.ui.theme.iOSGray5
import com.timetracker.app.ui.theme.iOSGray6
import com.timetracker.app.ui.theme.iOSGreen
import com.timetracker.app.ui.theme.iOSLabel
import com.timetracker.app.ui.theme.iOSRed
import com.timetracker.app.ui.theme.iOSSecondaryBackground
import com.timetracker.app.ui.theme.iOSSecondaryLabel
import com.timetracker.app.ui.theme.iOSSeparator
import com.timetracker.app.ui.theme.iOSSystemBackground
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddDialog(
    initialStartTime: LocalDateTime,
    templates: List<Template>,
    selectedDate: LocalDate,
    isFutureDate: Boolean = false,
    existingBlock: TimeBlock? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, LocalDateTime, LocalDateTime, String?, Boolean, TimeNature) -> Unit,
    onDelete: (() -> Unit)? = null,
    onAddPomodoro: ((String, String, LocalDateTime, Int, String?) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Determine if this is a past block
    val now = LocalDateTime.now()
    val isPastBlock = existingBlock?.let { block ->
        block.endTime.isBefore(now)
    } ?: false
    
    // Find matching template for existing block
    val matchingTemplate = existingBlock?.let { block ->
        templates.find { it.color == block.color && it.timeNature == block.timeNature }
    }
    
    var title by remember { mutableStateOf(existingBlock?.title ?: "") }
    var selectedTemplate by remember { mutableStateOf(matchingTemplate ?: templates.firstOrNull()) }
    var selectedColor by remember { mutableStateOf(existingBlock?.color ?: selectedTemplate?.color ?: "#5B9BD5") }
    var selectedTimeNature by remember { mutableStateOf(existingBlock?.timeNature ?: selectedTemplate?.timeNature ?: TimeNature.PRODUCTIVE) }
    var customMinutes by remember { mutableStateOf("") }
    var selectedDuration by remember { 
        mutableStateOf(
            if (existingBlock != null) {
                java.time.Duration.between(existingBlock.startTime, existingBlock.endTime).toMinutes().toInt()
            } else {
                selectedTemplate?.defaultDuration ?: 30
            }
        ) 
    }
    var note by remember { mutableStateOf(existingBlock?.note ?: "") }
    var isPomodoroEnabled by remember { mutableStateOf(false) }
    var pomodoroCycles by remember { mutableStateOf(1) }
    var isReminderEnabled by remember { mutableStateOf(existingBlock?.isReminderEnabled ?: false) }
    
    // Update color and time nature when template changes
    LaunchedEffect(selectedTemplate) {
        selectedTemplate?.let {
            selectedColor = it.color
            selectedTimeNature = it.timeNature
            if (title.isBlank() && existingBlock == null) {
                title = it.name
            }
        }
    }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // For existing blocks, use their original times; for new blocks, calculate based on duration
    val startTime = existingBlock?.startTime ?: initialStartTime
    val endTime = if (existingBlock != null) {
        // For existing blocks, use the original end time
        existingBlock.endTime
    } else {
        // For new blocks, calculate based on selected duration
        // 番茄钟模式不影响时间块时长，只启动服务
        remember(startTime, selectedDuration, customMinutes) {
            val duration = if (customMinutes.isNotBlank()) {
                customMinutes.toIntOrNull()?.coerceIn(15, 480) ?: selectedDuration
            } else {
                selectedDuration
            }
            startTime.plusMinutes(duration.toLong())
        }
    }

    // Get selected color
    val rawColor = try {
        Color(android.graphics.Color.parseColor(selectedColor))
    } catch (e: Exception) {
        iOSBlue
    }

    val displayColor = if (isLightColor(rawColor)) {
        rawColor.copy(red = rawColor.red * 0.7f, green = rawColor.green * 0.7f, blue = rawColor.blue * 0.7f)
    } else {
        rawColor
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingBlock == null) "添加时间块" else "编辑时间块",
                color = iOSLabel,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                // Time display - iOS style card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = displayColor.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = displayColor.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = displayColor
                        )
                        if (existingBlock == null) {
                            Text(
                                text = "时长: ${java.time.Duration.between(startTime, endTime).toMinutes()}分钟",
                                style = MaterialTheme.typography.bodySmall,
                                color = iOSSecondaryLabel
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // For past blocks, show simplified view
                if (isPastBlock) {
                    // Only show note for past blocks
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("备注") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = displayColor,
                            focusedLabelColor = displayColor
                        )
                    )
                } else {
                    // Full editing for new blocks and future blocks
                    // Duration selection (only for new blocks or future dates)
                    val canEditTime = existingBlock == null || isFutureDate
                    if (canEditTime) {
                        Text(
                            text = "选择时长",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = iOSLabel
                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset duration buttons - iOS style
                    val presetDurations = listOf(15, 30, 45, 60, 90, 120)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // First row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetDurations.take(3).forEach { duration ->
                                val isSelected = selectedDuration == duration && customMinutes.isBlank()
                                iOSDurationChip(
                                    duration = duration,
                                    isSelected = isSelected,
                                    categoryColor = displayColor,
                                    onClick = {
                                        selectedDuration = duration
                                        customMinutes = ""
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        // Second row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetDurations.drop(3).forEach { duration ->
                                val isSelected = selectedDuration == duration && customMinutes.isBlank()
                                iOSDurationChip(
                                    duration = duration,
                                    isSelected = isSelected,
                                    categoryColor = displayColor,
                                    onClick = {
                                        selectedDuration = duration
                                        customMinutes = ""
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom minutes input - iOS style
                    OutlinedTextField(
                        value = customMinutes,
                        onValueChange = { value ->
                            if (value.isEmpty() || (value.toIntOrNull() != null && value.length <= 3)) {
                                customMinutes = value
                                if (value.isNotBlank()) {
                                    selectedDuration = 0
                                }
                            }
                        },
                        label = { Text("自定义分钟") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = displayColor,
                            focusedLabelColor = displayColor
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pomodoro toggle - iOS style switch (新建时间块或编辑未来时间块时显示)
                    if (existingBlock == null || isFutureDate) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFFF6347))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🍅",
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "番茄钟模式",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = iOSLabel
                                    )
                                    Text(
                                        text = "25分钟专注 + 5分钟休息",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = iOSSecondaryLabel
                                    )
                                }
                            }
                            Switch(
                                checked = isPomodoroEnabled,
                                onCheckedChange = { isPomodoroEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFFF6347),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = iOSGray3
                            )
                        )
                    }

                    // Pomodoro cycles selection
                    AnimatedVisibility(visible = isPomodoroEnabled) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "循环次数: $pomodoroCycles 个番茄",
                                style = MaterialTheme.typography.labelMedium,
                                color = iOSLabel
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                (1..4).forEach { cycle ->
                                    val isSelected = pomodoroCycles == cycle
                                    iOSDurationChip(
                                        duration = cycle,
                                        isSelected = isSelected,
                                        categoryColor = Color(0xFFFF6347),
                                        onClick = { pomodoroCycles = cycle },
                                        modifier = Modifier.weight(1f),
                                        suffix = "个"
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val totalWorkMinutes = pomodoroCycles * 25
                            val totalBreakMinutes = (pomodoroCycles - 1).coerceAtLeast(0) * 5
                            val totalMinutes = totalWorkMinutes + totalBreakMinutes
                            Text(
                                text = "总计: ${totalWorkMinutes}分钟专注 + ${totalBreakMinutes}分钟休息 = ${totalMinutes}分钟",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF6347)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                } // End of if (existingBlock == null) for Pomodoro section

                // Reminder toggle for future dates
                if (isFutureDate) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = iOSBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "设置提醒",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = iOSLabel
                                    )
                                    Text(
                                        text = "在时间块开始前通知",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = iOSSecondaryLabel
                                    )
                                }
                            }
                            Switch(
                                checked = isReminderEnabled,
                                onCheckedChange = { isReminderEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = iOSBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = iOSGray3
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Template selection - iOS style (replaces title input)
                Text(
                    text = "选择模板",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = iOSLabel
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Template chips - 2 columns
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    templates.chunked(2).forEach { rowTemplates ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTemplates.forEach { template ->
                                val isSelected = selectedTemplate?.id == template.id
                                val templateColor = try {
                                    Color(android.graphics.Color.parseColor(template.color))
                                } catch (e: Exception) {
                                    iOSBlue
                                }
                                val displayTemplateColor = if (isLightColor(templateColor)) {
                                    templateColor.copy(red = templateColor.red * 0.7f, green = templateColor.green * 0.7f, blue = templateColor.blue * 0.7f)
                                } else templateColor

                                iOSFilterChip(
                                    selected = isSelected,
                                    onClick = { 
                                        selectedTemplate = template
                                    },
                                    label = template.name,
                                    color = displayTemplateColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowTemplates.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Productive/Unproductive toggle (only for new blocks)
                if (existingBlock == null) {
                    Text(
                        text = "类型",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = iOSLabel
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iOSFilterChip(
                            selected = selectedTimeNature == TimeNature.PRODUCTIVE,
                            onClick = { selectedTimeNature = TimeNature.PRODUCTIVE },
                            label = "元气满满",
                            color = Color(0xFF81C784),
                            modifier = Modifier.weight(1f)
                        )
                        iOSFilterChip(
                            selected = selectedTimeNature == TimeNature.UNPRODUCTIVE,
                            onClick = { selectedTimeNature = TimeNature.UNPRODUCTIVE },
                            label = "摸鱼时光",
                            color = Color(0xFFFF8A65),
                            modifier = Modifier.weight(1f)
                        )
                        iOSFilterChip(
                            selected = selectedTimeNature == TimeNature.NEUTRAL,
                            onClick = { selectedTimeNature = TimeNature.NEUTRAL },
                            label = "中性",
                            color = Color(0xFF90A4AE),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                    // Note - iOS style
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("备注（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 1,
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = displayColor,
                            focusedLabelColor = displayColor
                        )
                    )
                } // End of else block for non-past blocks
            }
        },
        confirmButton = {
            if (!isPastBlock) {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            if (isPomodoroEnabled && onAddPomodoro != null) {
                                // 番茄钟模式：只回调，不修改时间块
                                onAddPomodoro(title, selectedColor, startTime, pomodoroCycles, note.takeIf { it.isNotBlank() })
                            } else {
                                onConfirm(title, selectedColor, startTime, endTime, note.takeIf { it.isNotBlank() }, isReminderEnabled, selectedTimeNature)
                            }
                        }
                    },
                    enabled = title.isNotBlank() && endTime.isAfter(startTime.plusMinutes(14)),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPomodoroEnabled) Color(0xFFFF6347) else displayColor,
                        disabledContainerColor = iOSGray3
                    )
                ) {
                    Text(
                        if (isPomodoroEnabled) "添加${pomodoroCycles}个番茄" else "确定",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null && existingBlock != null) {
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = iOSRed
                        )
                    ) {
                        Text("删除")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(if (isPastBlock) "关闭" else "取消")
                }
            }
        },
        containerColor = iOSSystemBackground,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun iOSDurationChip(
    duration: Int,
    isSelected: Boolean,
    categoryColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    suffix: String = "分"
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) categoryColor else iOSGray5
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$duration$suffix",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (isSelected) Color.White else iOSLabel
        )
    }
}

@Composable
private fun iOSFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) color else iOSGray5
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) color else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (selected) Color.White else iOSLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun isLightColor(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance > 0.5f
}
