package com.timetracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.timetracker.app.data.model.Category
import com.timetracker.app.data.model.TimeBlock
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun TimeBlockItem(
    timeBlock: TimeBlock,
    category: Category?,
    hourHeight: Int,
    isDragging: Boolean = false,
    onDragStart: () -> Unit = {},
    onDrag: (deltaY: Float) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onResize: (newHeight: Float) -> Unit = {},
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    val durationMinutes = timeBlock.durationMinutes
    val heightDp = (durationMinutes / 60f * hourHeight).dp
    
    val backgroundColor = category?.color?.let { Color(android.graphics.Color.parseColor(it)) } 
        ?: MaterialTheme.colorScheme.primary
    
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 30.dp)
            .height(heightDp)
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor.copy(alpha = 0.9f))
            .border(
                width = if (isDragging) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
            )
            .pointerInput(timeBlock.id) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick() 
                    }
                )
            }
            .pointerInput(timeBlock.id) {
                detectDragGestures(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDragStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.y)
                    },
                    onDragEnd = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDragEnd()
                    }
                )
            }
            .zIndex(if (isDragging) 10f else 1f),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                text = timeBlock.title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "${timeBlock.startTime.format(timeFormatter)} - ${timeBlock.endTime.format(timeFormatter)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
            
            if (durationMinutes >= 45) {
                Spacer(modifier = Modifier.weight(1f))
                
                // Resize handle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .pointerInput(timeBlock.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onResize(dragAmount.y)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "调整大小",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TimeBlockPreview(
    title: String,
    color: Color,
    durationText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.9f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (durationText.isNotEmpty()) {
                Text(
                    text = durationText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 9.sp
                )
            }
        }
    }
}
