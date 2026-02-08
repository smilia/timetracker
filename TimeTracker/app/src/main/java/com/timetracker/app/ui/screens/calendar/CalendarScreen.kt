package com.timetracker.app.ui.screens.calendar

import android.view.View
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.timetracker.app.data.model.Template
import com.timetracker.app.data.model.TimeBlock
import com.timetracker.app.data.model.TimeNature
import com.timetracker.app.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val timeBlocks by viewModel.timeBlocks.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<TimeBlock?>(null) }
    var selectedTimeBlock by remember { mutableStateOf<TimeBlock?>(null) }
    var showMonthView by remember { mutableStateOf(false) }
    
    // 点击模板+点击格子交互模式 - 支持多选和拖选
    var selectedTemplate by remember { mutableStateOf<Template?>(null) }
    var isTemplateSelectionMode by remember { mutableStateOf(false) }
    var selectedSlots by remember { mutableStateOf<Set<Pair<Int, Int>>>(emptySet()) } // 已选择的格子 (hour, slot)
    var isDragSelecting by remember { mutableStateOf(false) } // 是否正在拖选
    
    // 拖拽状态
    var draggedTemplate by remember { mutableStateOf<Template?>(null) }
    var draggedBlock by remember { mutableStateOf<TimeBlock?>(null) }
    var dragPosition by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    
    // 日历边界
    var calendarBounds by remember { mutableStateOf(Rect.Zero) }
    
    // 悬停位置（高亮格子）
    var hoverHour by remember { mutableStateOf(-1) }
    var hoverSlot by remember { mutableStateOf(-1) }
    
    // 动画状态
    var animatingBlocks by remember { mutableStateOf<List<Triple<Int, Int, String>>>(emptyList()) }
    
    var clickedHour by remember { mutableStateOf(0) }
    var clickedMinute by remember { mutableStateOf(0) }

    val haptic = LocalHapticFeedback.current
    val hourHeight = 60
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        listState.scrollToItem(5)
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // 计算Y位置对应的小时和格子 - 修正版本（考虑LazyColumn滚动）
    fun getHourAndSlotFromY(y: Float): Pair<Int, Int> {
        if (calendarBounds == Rect.Zero) {
            android.util.Log.d("CalendarScreen", "calendarBounds is Zero, cannot calculate position")
            return Pair(-1, -1)
        }
        
        // 获取LazyColumn的滚动偏移（像素）
        val scrollOffset = listState.firstVisibleItemScrollOffset
        val firstVisibleItem = listState.firstVisibleItemIndex
        
        // 计算相对于日历顶部的位置（考虑滚动）
        val relativeY = y - calendarBounds.top + scrollOffset + (firstVisibleItem * hourHeight * context.resources.displayMetrics.density)
        
        // 每小时的高度（dp转px）
        val density = context.resources.displayMetrics.density
        val hourHeightPx = hourHeight * density
        
        // 计算小时和格子 - 确保结果在有效范围内
        val hour = kotlin.math.min(23, kotlin.math.max(0, (relativeY / hourHeightPx).toInt()))
        val minuteInHourPx = relativeY - (hour * hourHeightPx)
        val slot = kotlin.math.min(3, kotlin.math.max(0, (minuteInHourPx / (hourHeightPx / 4)).toInt()))
        
        android.util.Log.d("CalendarScreen", "getHourAndSlotFromY: y=$y, bounds.top=${calendarBounds.top}, scrollOffset=$scrollOffset, firstVisibleItem=$firstVisibleItem, relativeY=$relativeY, hour=$hour, slot=$slot")
        
        return Pair(hour, slot)
    }
    
    // 根据小时和格子计算Y坐标（用于高亮预览）- 考虑LazyColumn滚动
    fun getYFromHourAndSlot(hour: Int, slot: Int): Float {
        val density = context.resources.displayMetrics.density
        val hourHeightPx = hourHeight * density
        
        // 获取LazyColumn的滚动偏移（像素）
        val scrollOffset = listState.firstVisibleItemScrollOffset
        val firstVisibleItem = listState.firstVisibleItemIndex
        
        // 计算高亮位置（考虑滚动）
        return calendarBounds.top - scrollOffset - (firstVisibleItem * hourHeight * density) + (hour * hourHeightPx) + (slot * hourHeightPx / 4)
    }

    // 处理模板放置（带动画）- 不清除选择状态，支持多选
    fun placeTemplateWithAnimation(template: Template, hour: Int, slot: Int) {
        val minute = slot * 15
        val startTime = selectedDate.atTime(hour, minute)
        val endTime = startTime.plusMinutes(template.defaultDuration.toLong())
        
        // 添加动画状态
        val newAnimating = Triple(hour, slot, template.color)
        animatingBlocks = animatingBlocks + newAnimating
        
        // 添加时间块
        viewModel.addTimeBlockFromTemplate(template, startTime, endTime)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        
        // 动画结束后清理
        coroutineScope.launch {
            kotlinx.coroutines.delay(500)
            animatingBlocks = animatingBlocks.filterNot { it == newAnimating }
        }
    }
    
    // 批量放置模板到已选择的格子
    fun placeTemplateToSelectedSlots() {
        selectedTemplate?.let { template ->
            selectedSlots.forEach { (hour, slot) ->
                placeTemplateWithAnimation(template, hour, slot)
            }
            // 清空已选择格子
            selectedSlots = emptySet()
            isDragSelecting = false
        }
    }

    // 检查是否在日历区域内 - 简化版本
    fun isInCalendarArea(x: Float, y: Float): Boolean {
        if (calendarBounds == Rect.Zero) return false
        // 只要Y在日历范围内就认为在日历区域（简化判断）
        return y >= calendarBounds.top && y <= calendarBounds.bottom
    }

    // 处理模板拖拽放下
    fun handleTemplateDrop(template: Template, dropY: Float, dropX: Float) {
        android.util.Log.d("CalendarScreen", "handleTemplateDrop: dropY=$dropY, dropX=$dropX, calendarBounds=$calendarBounds")
        val (hour, slot) = getHourAndSlotFromY(dropY)
        android.util.Log.d("CalendarScreen", "handleTemplateDrop: calculated hour=$hour, slot=$slot")
        if (hour >= 0 && slot >= 0 && isInCalendarArea(dropX, dropY)) {
            placeTemplateWithAnimation(template, hour, slot)
            // 拖拽后退出选择模式
            selectedTemplate = null
            isTemplateSelectionMode = false
        }
        hoverHour = -1
        hoverSlot = -1
    }

    // 处理时间块移动
    fun handleBlockDrop(block: TimeBlock, dropY: Float, dropX: Float) {
        val (hour, slot) = getHourAndSlotFromY(dropY)
        if (hour >= 0 && slot >= 0 && isInCalendarArea(dropX, dropY)) {
            val minute = slot * 15
            val newStartTime = selectedDate.atTime(hour, minute)
            viewModel.moveTimeBlock(block, newStartTime)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        hoverHour = -1
        hoverSlot = -1
    }

    // 处理格子点击（模板选择模式）- 支持多选和拖选
    fun handleSlotClick(hour: Int, minute: Int) {
        if (isTemplateSelectionMode && selectedTemplate != null) {
            val slot = minute / 15
            val slotKey = Pair(hour, slot)
            if (isDragSelecting) {
                // 拖选模式：切换选择状态
                selectedSlots = if (selectedSlots.contains(slotKey)) {
                    selectedSlots - slotKey
                } else {
                    selectedSlots + slotKey
                }
            } else {
                // 普通点击模式：直接放置
                placeTemplateWithAnimation(selectedTemplate!!, hour, slot)
            }
        }
        // 普通模式下点击空白格子不弹出任何对话框
    }
    
    // 处理拖选开始 - 可以从任意位置开始
    fun handleDragSelectionStart(position: Offset) {
        if (isTemplateSelectionMode && selectedTemplate != null) {
            // 检查是否在日历区域内
            if (!isInCalendarArea(position.x, position.y)) {
                return
            }
            val (hour, slot) = getHourAndSlotFromY(position.y)
            if (hour >= 0 && slot >= 0) {
                isDragSelecting = true
                selectedSlots = setOf(Pair(hour, slot))
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        }
    }
    
    // 处理拖选移动 - 根据Y坐标计算当前在哪个格子
    fun handleDragSelectionMove(position: Offset) {
        if (isDragSelecting && isTemplateSelectionMode) {
            // 检查是否在日历区域内
            if (!isInCalendarArea(position.x, position.y)) {
                return
            }
            val (hour, slot) = getHourAndSlotFromY(position.y)
            if (hour >= 0 && slot >= 0) {
                val slotKey = Pair(hour, slot)
                if (!selectedSlots.contains(slotKey)) {
                    selectedSlots = selectedSlots + slotKey
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
        }
    }
    
    // 处理拖选结束
    fun handleDragSelectionEnd() {
        if (isDragSelecting) {
            // 放置到所有选中的格子
            placeTemplateToSelectedSlots()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        modifier = Modifier.clickable { showMonthView = true }
                    ) {
                        Text(
                            text = "日程",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")),
                                style = MaterialTheme.typography.bodySmall,
                                color = iOSSecondaryLabel
                            )
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "选择日期",
                                modifier = Modifier.size(16.dp),
                                tint = iOSSecondaryLabel
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.selectDate(selectedDate.minusDays(1)) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "前一天")
                    }
                },
                actions = {
                    // 启动番茄钟按钮
                    IconButton(
                        onClick = {
                            // 启动番茄钟服务
                            com.timetracker.app.service.pomodoro.PomodoroService.startPomodoro(
                                context,
                                "番茄钟",
                                1
                            )
                            Toast.makeText(context, "🍅 番茄钟已启动", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = "启动番茄钟",
                            tint = Color(0xFFFF6347)
                        )
                    }
                    
                    IconButton(onClick = { viewModel.selectDate(selectedDate.plusDays(1)) }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "后一天")
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = iOSSystemBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(iOSSystemBackground)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // 日历区域
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = iOSSystemBackground
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    CalendarGrid(
                        timeBlocks = timeBlocks,
                        hourHeight = hourHeight,
                        listState = listState,
                        hoverHour = hoverHour,
                        hoverSlot = hoverSlot,
                        animatingBlocks = animatingBlocks,
                        selectedSlots = selectedSlots,
                        isTemplateSelectionMode = isTemplateSelectionMode,
                        isDragSelecting = isDragSelecting,
                        onEmptySlotClick = { hour, minute -> handleSlotClick(hour, minute) },
                        onDragSelectionStart = { position -> handleDragSelectionStart(position) },
                        onDragSelectionMove = { position -> handleDragSelectionMove(position) },
                        onDragSelectionEnd = { handleDragSelectionEnd() },
                        onBlockClick = { block ->
                            selectedTimeBlock = block
                            // 设置点击的小时和分钟，用于计算 isFutureDate
                            clickedHour = block.startTime.hour
                            clickedMinute = block.startTime.minute
                            showAddDialog = true
                        },
                        onBlockLongPress = { block, offset ->
                            draggedBlock = block
                            dragPosition = offset
                            isDragging = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // 立即更新高亮位置
                            val (hour, slot) = getHourAndSlotFromY(offset.y)
                            hoverHour = hour
                            hoverSlot = slot
                        },
                        onBoundsChange = { bounds ->
                            calendarBounds = bounds
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 模板侧边栏
                Card(
                    modifier = Modifier
                        .width(90.dp)
                        .fillMaxHeight()
                        .padding(end = 8.dp, top = 8.dp, bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = iOSSecondaryBackground
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    TemplateSidebar(
                        templates = templates,
                        selectedTemplate = selectedTemplate,
                        isSelectionMode = isTemplateSelectionMode,
                        onTemplateClick = { template ->
                            if (isTemplateSelectionMode && selectedTemplate?.id == template.id) {
                                // 取消选择
                                selectedTemplate = null
                                isTemplateSelectionMode = false
                                selectedSlots = emptySet()
                                isDragSelecting = false
                            } else {
                                // 进入选择模式
                                selectedTemplate = template
                                isTemplateSelectionMode = true
                                selectedSlots = emptySet()
                                isDragSelecting = false
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        },
                        onTemplateDragStart = { template, offset ->
                            android.util.Log.d("CalendarScreen", "onTemplateDragStart: offset=$offset, calendarBounds=$calendarBounds")
                            draggedTemplate = template
                            dragPosition = offset
                            isDragging = true
                            // 立即更新高亮位置 - 使用全局坐标
                            val (hour, slot) = getHourAndSlotFromY(offset.y)
                            android.util.Log.d("CalendarScreen", "onTemplateDragStart: hour=$hour, slot=$slot")
                            hoverHour = hour
                            hoverSlot = slot
                        },
                        onDragUpdate = { offset ->
                            dragPosition = offset
                            // 更新高亮位置 - 使用全局坐标
                            val (hour, slot) = getHourAndSlotFromY(offset.y)
                            android.util.Log.d("CalendarScreen", "onDragUpdate: y=${offset.y}, hour=$hour, slot=$slot")
                            if (hour != hoverHour || slot != hoverSlot) {
                                hoverHour = hour
                                hoverSlot = slot
                            }
                        },
                        onDragEnd = { offset ->
                            isDragging = false
                            draggedTemplate?.let { template ->
                                handleTemplateDrop(template, offset.y, offset.x)
                                draggedTemplate = null
                            }
                            draggedBlock?.let { block ->
                                handleBlockDrop(block, offset.y, offset.x)
                                draggedBlock = null
                            }
                            hoverHour = -1
                            hoverSlot = -1
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // 拖拽预览 - 显示在手指位置
            if (isDragging) {
                android.util.Log.d("CalendarScreen", "DragPreview: dragPosition=$dragPosition, hoverHour=$hoverHour, hoverSlot=$hoverSlot")
                val density = context.resources.displayMetrics.density
                // 模板大小：宽度与日历格子一致，高度与模板卡片一致（60dp）
                val templateWidthPx = ((calendarBounds.right - calendarBounds.left) - 48f * density) / 4f
                val templateHeightPx = 60f * density
                draggedTemplate?.let { template ->
                    TemplateDragItem(
                        template = template,
                        position = dragPosition,
                        slotWidth = templateWidthPx,
                        slotHeight = templateHeightPx
                    )
                }
                draggedBlock?.let { block ->
                    BlockDragItem(
                        block = block,
                        position = dragPosition,
                        slotWidth = templateWidthPx,
                        slotHeight = templateHeightPx
                    )
                }
            }
            
            // 错误消息
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .zIndex(200f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = iOSRed.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = message,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // 对话框
    if (showAddDialog) {
        val initialStartTime = selectedDate.atTime(clickedHour, clickedMinute)
        val isFutureDate = selectedDate.isAfter(LocalDate.now()) || 
            (selectedDate.isEqual(LocalDate.now()) && initialStartTime.isAfter(LocalDateTime.now()))
        
        QuickAddDialog(
            initialStartTime = initialStartTime,
            templates = templates,
            selectedDate = selectedDate,
            isFutureDate = isFutureDate,
            existingBlock = selectedTimeBlock,
            onDismiss = {
                showAddDialog = false
                selectedTimeBlock = null
            },
            onConfirm = { title, color, startTime, endTime, note, isReminderEnabled, timeNature ->
                if (selectedTimeBlock != null) {
                    viewModel.updateTimeBlock(
                        selectedTimeBlock!!.copy(
                            title = title,
                            color = color,
                            startTime = startTime,
                            endTime = endTime,
                            note = note,
                            timeNature = timeNature
                        )
                    )
                } else {
                    viewModel.addTimeBlock(
                        title = title,
                        color = color,
                        startTime = startTime,
                        endTime = endTime,
                        note = note,
                        timeNature = timeNature
                    )
                }
                showAddDialog = false
                selectedTimeBlock = null
            },
            onDelete = if (selectedTimeBlock != null) {{
                showDeleteConfirm = selectedTimeBlock
                showAddDialog = false
            }} else null,
            onAddPomodoro = { title, color, startTime, cycles, note ->
                // 番茄钟模式：只启动服务，不创建时间块
                // 时间块保持原样（15分钟或其他时长）
                com.timetracker.app.service.pomodoro.PomodoroService.startPomodoro(
                    context,
                    title,
                    cycles
                )
                showAddDialog = false
                selectedTimeBlock = null
            }
        )
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除确认") },
            text = { Text("确定要删除这个时间块吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTimeBlock(showDeleteConfirm!!)
                        showDeleteConfirm = null
                    }
                ) {
                    Text("删除", color = iOSRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 月览日程
    if (showMonthView) {
        MonthViewDialog(
            selectedDate = selectedDate,
            timeBlocks = timeBlocks,
            onDateSelect = { date ->
                viewModel.selectDate(date)
                showMonthView = false
            },
            onDismiss = { showMonthView = false }
        )
    }
}

// 拖拽覆盖层
@Composable
private fun DragOverlay(
    template: Template?,
    block: TimeBlock?,
    position: Offset,
    calendarBounds: Rect,
    hourHeight: Int,
    onPositionChange: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    // 使用本地密度转换 dp 到 px
    val density = androidx.compose.ui.platform.LocalDensity.current
    val slotWidthPx = with(density) { 80.dp.toPx() }
    val slotHeightPx = with(density) { 60.dp.toPx() }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newPosition = Offset(
                            position.x + dragAmount.x,
                            position.y + dragAmount.y
                        )
                        onPositionChange(newPosition)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            }
            .zIndex(100f)
    ) {
        // 拖拽项显示在手指正下方（手指中心对准拖拽项中心）
        template?.let {
            TemplateDragItem(
                template = it,
                position = position,
                slotWidth = slotWidthPx,
                slotHeight = slotHeightPx,
                modifier = Modifier.zIndex(100f)
            )
        }
        
        block?.let {
            BlockDragItem(
                block = it,
                position = position,
                slotWidth = slotWidthPx,
                slotHeight = slotHeightPx,
                modifier = Modifier.zIndex(100f)
            )
        }
    }
}

@Composable
private fun TemplateDragItem(
    template: Template,
    position: Offset,
    slotWidth: Float,
    slotHeight: Float,
    modifier: Modifier = Modifier
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(template.color))
    } catch (e: Exception) {
        iOSBlue
    }

    // 将像素转换为 dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val widthDp = with(density) { slotWidth.toDp() }
    val heightDp = with(density) { slotHeight.toDp() }
    
    // 获取根布局的偏移量
    var rootOffset by remember { mutableStateOf(Offset.Zero) }

    // 模板显示在手指正下方（手指中心对准预览中心）
    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                rootOffset = coordinates.positionInRoot()
            }
    ) {
        Box(
            modifier = Modifier
                .offset { 
                    // 将全局坐标转换为相对于根布局的本地坐标
                    val localX = position.x - rootOffset.x - slotWidth / 2
                    val localY = position.y - rootOffset.y - slotHeight / 2
                    IntOffset(
                        x = localX.roundToInt(),
                        y = localY.roundToInt()
                    ) 
                }
                .width(widthDp)
                .height(heightDp)
                .shadow(12.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .border(3.dp, Color.White, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}

@Composable
private fun BlockDragItem(
    block: TimeBlock,
    position: Offset,
    slotWidth: Float,
    slotHeight: Float,
    modifier: Modifier = Modifier
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(block.color))
    } catch (e: Exception) {
        iOSBlue
    }

    // 将像素转换为 dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val widthDp = with(density) { slotWidth.toDp() }

    // 块显示在手指正下方（手指中心对准预览中心）
    // 根据块的实际时长计算高度
    val durationMinutes = java.time.Duration.between(block.startTime, block.endTime).toMinutes()
    val heightMultiplier = (durationMinutes / 15.0).coerceAtLeast(1.0).toFloat()
    val actualHeightPx = slotHeight * heightMultiplier
    val actualHeightDp = with(density) { actualHeightPx.toDp() }
    
    // 获取根布局的偏移量
    var rootOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                rootOffset = coordinates.positionInRoot()
            }
    ) {
        Box(
            modifier = Modifier
                .offset { 
                    // 将全局坐标转换为相对于根布局的本地坐标
                    val localX = position.x - rootOffset.x - slotWidth / 2
                    val localY = position.y - rootOffset.y - actualHeightPx / 2
                    IntOffset(
                        x = localX.roundToInt(),
                        y = localY.roundToInt()
                    ) 
                }
                .width(widthDp)
                .height(actualHeightDp)
                .shadow(12.dp, RoundedCornerShape(6.dp))
                .clip(RoundedCornerShape(6.dp))
                .background(backgroundColor)
                .border(3.dp, Color.White, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = block.title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    timeBlocks: List<TimeBlock>,
    hourHeight: Int,
    listState: androidx.compose.foundation.lazy.LazyListState,
    hoverHour: Int,
    hoverSlot: Int,
    animatingBlocks: List<Triple<Int, Int, String>>,
    selectedSlots: Set<Pair<Int, Int>>,
    isTemplateSelectionMode: Boolean,
    isDragSelecting: Boolean,
    onEmptySlotClick: (Int, Int) -> Unit,
    onDragSelectionStart: (Offset) -> Unit,
    onDragSelectionMove: (Offset) -> Unit,
    onDragSelectionEnd: () -> Unit,
    onBlockClick: (TimeBlock) -> Unit,
    onBlockLongPress: (TimeBlock, Offset) -> Unit,
    onBoundsChange: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    var gridBounds by remember { mutableStateOf(Rect.Zero) }
    var localDragSelecting by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                val size = coordinates.size
                gridBounds = Rect(
                    position.x,
                    position.y,
                    position.x + size.width,
                    position.y + size.height
                )
                android.util.Log.d("CalendarScreen", "CalendarGrid onGloballyPositioned: position=$position, size=$size, gridBounds=$gridBounds")
                onBoundsChange(gridBounds)
            }
            // 拖选手势 - 只在选择模式下启用
            .pointerInput(isTemplateSelectionMode) {
                if (isTemplateSelectionMode) {
                    detectDragGestures(
                        onDragStart = { startPosition ->
                            // 转换为全局坐标
                            val globalPosition = Offset(
                                gridBounds.left + startPosition.x,
                                gridBounds.top + startPosition.y
                            )
                            localDragSelecting = true
                            onDragSelectionStart(globalPosition)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val position = change.position
                            // 转换为全局坐标
                            val globalPosition = Offset(
                                gridBounds.left + position.x,
                                gridBounds.top + position.y
                            )
                            onDragSelectionMove(globalPosition)
                        },
                        onDragEnd = {
                            localDragSelecting = false
                            onDragSelectionEnd()
                        },
                        onDragCancel = {
                            localDragSelecting = false
                            onDragSelectionEnd()
                        }
                    )
                }
            }
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            items(24, key = { it }) { hour ->
                val hourBlocks = remember(timeBlocks, hour) {
                    timeBlocks.filter { it.startTime.hour == hour }
                }
                val hourAnimatingSlots = remember(animatingBlocks, hour) {
                    animatingBlocks.filter { it.first == hour }.map { it.second to it.third }
                }
                val hourSelectedSlots = remember(selectedSlots, hour) {
                    selectedSlots.filter { it.first == hour }.map { it.second }.toSet()
                }
                
                HourRow(
                    hour = hour,
                    hourHeight = hourHeight,
                    blocks = hourBlocks,
                    isHoverHour = hour == hoverHour,
                    hoverSlot = if (hour == hoverHour) hoverSlot else -1,
                    animatingSlots = hourAnimatingSlots,
                    selectedSlots = hourSelectedSlots,
                    isTemplateSelectionMode = isTemplateSelectionMode,
                    isDragSelecting = isDragSelecting || localDragSelecting,
                    onEmptySlotClick = { minute -> onEmptySlotClick(hour, minute) },
                    onBlockClick = onBlockClick,
                    onBlockLongPress = { block, offset -> onBlockLongPress(block, offset) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HourRow(
    hour: Int,
    hourHeight: Int,
    blocks: List<TimeBlock>,
    isHoverHour: Boolean,
    hoverSlot: Int,
    animatingSlots: List<Pair<Int, String>>,
    selectedSlots: Set<Int>,
    isTemplateSelectionMode: Boolean,
    isDragSelecting: Boolean,
    onEmptySlotClick: (Int) -> Unit,
    onBlockClick: (TimeBlock) -> Unit,
    onBlockLongPress: (TimeBlock, Offset) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(hourHeight.dp)
            .background(iOSSystemBackground)
    ) {
        // 小时标签 - 优化样式
        Box(
            modifier = Modifier
                .width(48.dp)
                .fillMaxHeight()
                .background(iOSSecondaryBackground),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                text = String.format("%02d:00", hour),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = iOSSecondaryLabel,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        
        // 4个15分钟格子
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            for (slot in 0..3) {
                val slotStartMinute = slot * 15
                val isHovered = isHoverHour && slot == hoverSlot
                val isSelected = selectedSlots.contains(slot)
                
                // 使用 remember 缓存 blocks 过滤结果
                val slotBlocks = remember(blocks, slot) {
                    blocks.filter { block ->
                        val blockStartMinute = block.startTime.minute
                        blockStartMinute >= slotStartMinute && blockStartMinute < slotStartMinute + 15
                    }
                }
                
                val animatingColor = remember(animatingSlots, slot) {
                    animatingSlots.find { it.first == slot }?.second
                }
                
                // 缓存背景色计算
                val backgroundColor = remember(animatingColor, isSelected, isHovered, isTemplateSelectionMode) {
                    when {
                        animatingColor != null -> try {
                            Color(android.graphics.Color.parseColor(animatingColor)).copy(alpha = 0.4f)
                        } catch (e: Exception) {
                            iOSBlue.copy(alpha = 0.4f)
                        }
                        isSelected -> Color(0xFF007AFF).copy(alpha = 0.3f)
                        isHovered -> Color(0xFF007AFF).copy(alpha = 0.15f)
                        isTemplateSelectionMode -> Color(0xFF007AFF).copy(alpha = 0.05f)
                        else -> Color.Transparent
                    }
                }
                
                // 缓存边框色
                val borderColor = remember(isSelected, isHovered) {
                    when {
                        isSelected -> Color(0xFF007AFF)
                        isHovered -> Color(0xFF007AFF).copy(alpha = 0.6f)
                        else -> iOSSeparator
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(width = 0.5.dp, color = borderColor)
                        .background(backgroundColor)
                        .clickable { onEmptySlotClick(slotStartMinute) },
                    contentAlignment = Alignment.TopStart
                ) {
                    // 显示该格子中的时间块 - 只显示第一个
                    slotBlocks.firstOrNull()?.let { block ->
                        TimeBlockChip(
                            block = block,
                            onClick = { onBlockClick(block) },
                            onLongPress = { offset -> onBlockLongPress(block, offset) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    // 显示选中标记 - 优化为右上角小圆点
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF007AFF))
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✓",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeBlockChip(
    block: TimeBlock,
    onClick: () -> Unit,
    onLongPress: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(block.color))
    } catch (e: Exception) {
        iOSBlue
    }
    
    val haptic = LocalHapticFeedback.current
    var chipBounds by remember { mutableStateOf(Rect.Zero) }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 1.dp)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                val size = coordinates.size
                chipBounds = Rect(
                    position.x,
                    position.y,
                    position.x + size.width,
                    position.y + size.height
                )
            }
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    // 传递块中心的全局坐标
                    val centerX = chipBounds.center.x
                    val centerY = chipBounds.center.y
                    onLongPress(Offset(centerX, centerY))
                }
            )
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(2.dp)
        ) {
            Text(
                text = block.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            
            // 时间块不显示时间，只显示标题
        }
    }
}

@Composable
private fun TemplateSidebar(
    templates: List<Template>,
    selectedTemplate: Template?,
    isSelectionMode: Boolean,
    onTemplateClick: (Template) -> Unit,
    onTemplateDragStart: (Template, Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    // 按使用次数排序
    val sortedTemplates = remember(templates) {
        templates.sortedByDescending { it.usageCount }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 8.dp)
    ) {
        // 标题栏带图标
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                text = "模板",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = iOSLabel
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "(${sortedTemplates.size})",
                style = MaterialTheme.typography.labelSmall,
                color = iOSSecondaryLabel
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(sortedTemplates) { template ->
                val isSelected = isSelectionMode && selectedTemplate?.id == template.id
                TemplateChip(
                    template = template,
                    isSelected = isSelected,
                    onClick = { onTemplateClick(template) },
                    onDragStart = { offset -> onTemplateDragStart(template, offset) },
                    onDragUpdate = onDragUpdate,
                    onDragEnd = onDragEnd
                )
            }
        }
    }
}

@Composable
private fun TemplateChip(
    template: Template,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragUpdate: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(template.color))
    } catch (e: Exception) {
        iOSBlue
    }
    
    val haptic = LocalHapticFeedback.current
    var currentPosition by remember { mutableStateOf(Offset.Zero) }
    var globalOffset by remember { mutableStateOf(Offset.Zero) }
    
    // 与左侧格子大小一致：80dp x 60dp
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(60.dp)
            .onGloballyPositioned { coordinates ->
                globalOffset = coordinates.positionInRoot()
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { startOffset ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // 长按后开始拖拽，startOffset 是相对于组件的本地坐标
                        // 转换为全局坐标
                        currentPosition = Offset(
                            globalOffset.x + startOffset.x,
                            globalOffset.y + startOffset.y
                        )
                        android.util.Log.d("CalendarScreen", "TemplateChip onDragStart: startOffset=$startOffset, globalOffset=$globalOffset, currentPosition=$currentPosition")
                        onDragStart(currentPosition)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentPosition = Offset(
                            currentPosition.x + dragAmount.x,
                            currentPosition.y + dragAmount.y
                        )
                        onDragUpdate(currentPosition)
                    },
                    onDragEnd = {
                        onDragEnd(currentPosition)
                    },
                    onDragCancel = {
                        onDragEnd(currentPosition)
                    }
                )
            }
            .clickable(onClick = onClick)
            .shadow(if (isSelected) 6.dp else 2.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = if (isSelected) 2.5.dp else 0.5.dp,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // 只显示模板名称，不显示时间和绿点
        Text(
            text = template.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp)
        )
    }
}

// 月览日程对话框
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthViewDialog(
    selectedDate: LocalDate,
    timeBlocks: List<TimeBlock>,
    onDateSelect: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val haptic = LocalHapticFeedback.current
    
    // iOS风格对话框
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = iOSSystemBackground
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // 标题栏 - iOS风格
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 上个月按钮
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(iOSGray6)
                                .clickable { 
                                    currentMonth = currentMonth.minusMonths(1)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "上个月",
                                tint = iOSBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        // 月份标题
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentMonth.format(DateTimeFormatter.ofPattern("yyyy年")),
                                style = MaterialTheme.typography.labelMedium,
                                color = iOSSecondaryLabel
                            )
                            Text(
                                text = currentMonth.format(DateTimeFormatter.ofPattern("MM月")),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = iOSLabel
                            )
                        }
                        
                        // 下个月按钮
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(iOSGray6)
                                .clickable { 
                                    currentMonth = currentMonth.plusMonths(1)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "下个月",
                                tint = iOSBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 星期标题 - iOS风格
                    Row(modifier = Modifier.fillMaxWidth()) {
                        val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
                        weekDays.forEachIndexed { index, day ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = if (index == 0 || index == 6) iOSRed else iOSSecondaryLabel
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // 日历网格
                    val firstDayOfMonth = currentMonth.atDay(1)
                    val daysInMonth = currentMonth.lengthOfMonth()
                    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
                    
                    Column {
                        var dayCounter = 1 - firstDayOfWeek
                        repeat(6) { week ->
                            if (dayCounter <= daysInMonth) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(7) { dayOfWeek ->
                                        val day = dayCounter
                                        if (day in 1..daysInMonth) {
                                            val date = currentMonth.atDay(day)
                                            val dayBlocks = timeBlocks.filter { 
                                                it.startTime.toLocalDate() == date 
                                            }
                                            val hasBlocks = dayBlocks.isNotEmpty()
                                            val isSelected = date == selectedDate
                                            val isToday = date == LocalDate.now()
                                            val isWeekend = dayOfWeek == 0 || dayOfWeek == 6
                                            
                                            // 计算该日期的时间块颜色分布
                                            val blockColors = dayBlocks
                                                .take(3)
                                                .map { block ->
                                                    try {
                                                        Color(android.graphics.Color.parseColor(block.color))
                                                    } catch (e: Exception) {
                                                        iOSBlue
                                                    }
                                                }
                                            
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(0.85f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        when {
                                                            isSelected -> iOSBlue
                                                            else -> Color.Transparent
                                                        }
                                                    )
                                                    .border(
                                                        width = if (isToday && !isSelected) 2.dp else 0.dp,
                                                        color = if (isToday && !isSelected) iOSBlue else Color.Transparent,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable { 
                                                        onDateSelect(date)
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    // 日期数字
                                                    Text(
                                                        text = day.toString(),
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                                        ),
                                                        color = when {
                                                            isSelected -> Color.White
                                                            isToday -> iOSBlue
                                                            isWeekend -> iOSRed
                                                            else -> iOSLabel
                                                        }
                                                    )
                                                    
                                                    // 时间块指示器
                                                    if (hasBlocks) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Row(
                                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                        ) {
                                                            if (blockColors.isEmpty()) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(5.dp)
                                                                        .clip(CircleShape)
                                                                        .background(
                                                                            if (isSelected) Color.White.copy(alpha = 0.8f) else iOSBlue
                                                                        )
                                                                )
                                                            } else {
                                                                blockColors.forEach { color ->
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(5.dp)
                                                                            .clip(CircleShape)
                                                                            .background(
                                                                                if (isSelected) Color.White else color
                                                                            )
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                        dayCounter++
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 底部统计信息
                    val monthBlocks = timeBlocks.filter { 
                        it.startTime.toLocalDate().month == currentMonth.month &&
                        it.startTime.toLocalDate().year == currentMonth.year
                    }
                    val productiveTime = monthBlocks
                        .filter { it.timeNature == TimeNature.PRODUCTIVE }
                        .sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() }
                    val unproductiveTime = monthBlocks
                        .filter { it.timeNature == TimeNature.UNPRODUCTIVE }
                        .sumOf { java.time.Duration.between(it.startTime, it.endTime).toMinutes() }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // 元气满满统计
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${productiveTime / 60}h ${productiveTime % 60}m",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF81C784)
                            )
                            Text(
                                text = "元气满满",
                                style = MaterialTheme.typography.labelSmall,
                                color = iOSSecondaryLabel
                            )
                        }
                        
                        // 摸鱼时光统计
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${unproductiveTime / 60}h ${unproductiveTime % 60}m",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFFF8A65)
                            )
                            Text(
                                text = "摸鱼时光",
                                style = MaterialTheme.typography.labelSmall,
                                color = iOSSecondaryLabel
                            )
                        }
                        
                        // 记录天数
                        val recordedDays = monthBlocks
                            .map { it.startTime.toLocalDate() }
                            .distinct()
                            .count()
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$recordedDays",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = iOSBlue
                            )
                            Text(
                                text = "记录天数",
                                style = MaterialTheme.typography.labelSmall,
                                color = iOSSecondaryLabel
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 关闭按钮 - iOS风格
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = iOSBlue
                            )
                        ) {
                            Text(
                                "关闭",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
