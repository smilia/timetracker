package com.timetracker.app.ui.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timetracker.app.data.model.Template
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateTimePickerDialog(
    template: Template,
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime, LocalDateTime) -> Unit
) {
    var startTime by remember {
        mutableStateOf(LocalDateTime.of(selectedDate, LocalTime.of(9, 0)))
    }
    var endTime by remember {
        mutableStateOf(LocalDateTime.of(selectedDate, LocalTime.of(9, 0)).plusMinutes(template.defaultDuration.toLong()))
    }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${template.name} - 选择时间") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "时长: ${template.defaultDuration}分钟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Time selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Start time
                    OutlinedButton(
                        onClick = { showStartTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Schedule, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(startTime.format(timeFormatter))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text("至", modifier = Modifier.padding(top = 8.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    // End time
                    OutlinedButton(
                        onClick = { showEndTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Schedule, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(endTime.format(timeFormatter))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(startTime, endTime) },
                enabled = endTime.isAfter(startTime)
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // Time picker dialogs
    if (showStartTimePicker) {
        TimePickerDialog(
            initialTime = startTime.toLocalTime(),
            onDismiss = { showStartTimePicker = false },
            onConfirm = { newTime ->
                val newStartTime = LocalDateTime.of(selectedDate, newTime)
                startTime = newStartTime
                endTime = newStartTime.plusMinutes(template.defaultDuration.toLong())
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            initialTime = endTime.toLocalTime(),
            onDismiss = { showEndTimePicker = false },
            onConfirm = { newTime ->
                val newEndTime = LocalDateTime.of(selectedDate, newTime)
                if (newEndTime.isAfter(startTime)) {
                    endTime = newEndTime
                }
                showEndTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(LocalTime.of(timePickerState.hour, timePickerState.minute))
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
