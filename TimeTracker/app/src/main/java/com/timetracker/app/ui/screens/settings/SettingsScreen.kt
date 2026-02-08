package com.timetracker.app.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.timetracker.app.service.notification.IntervalReminderManager
import com.timetracker.app.service.notification.EnhancedNotificationService
import com.timetracker.app.service.overlay.AtomicNotificationService
import com.timetracker.app.ui.theme.iOSBlue
import com.timetracker.app.ui.theme.iOSGray6
import com.timetracker.app.ui.theme.iOSGreen
import com.timetracker.app.ui.theme.iOSLabel
import com.timetracker.app.ui.theme.iOSRed
import com.timetracker.app.ui.theme.iOSSecondaryBackground
import com.timetracker.app.ui.theme.iOSSecondaryLabel
import com.timetracker.app.ui.theme.iOSSystemBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // 权限状态
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    
    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }
    
    // 服务状态
    var isEnhancedNotificationEnabled by remember { mutableStateOf(false) }
    var isAtomicNotificationEnabled by remember { mutableStateOf(false) }
    
    // 权限请求启动器
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "设置",
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
                .verticalScroll(scrollState)
        ) {
            // 通知设置组
            SettingsSection(title = "通知设置") {
                // 通知权限
                PermissionSettingItem(
                    title = "通知权限",
                    subtitle = if (hasNotificationPermission) "已授权" else "需要授权以显示通知",
                    icon = Icons.Default.Notifications,
                    isGranted = hasNotificationPermission,
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                )
                
                // 增强通知服务
                ToggleSettingItem(
                    title = "增强通知",
                    subtitle = "显示当前时间块、下一个安排和今日统计",
                    icon = Icons.Default.NotificationsActive,
                    isEnabled = isEnhancedNotificationEnabled && hasNotificationPermission,
                    onToggle = { enabled ->
                        isEnhancedNotificationEnabled = enabled
                        if (enabled) {
                            val intent = Intent(context, EnhancedNotificationService::class.java).apply {
                                action = EnhancedNotificationService.ACTION_START
                            }
                            context.startForegroundService(intent)
                        } else {
                            val intent = Intent(context, EnhancedNotificationService::class.java).apply {
                                action = EnhancedNotificationService.ACTION_STOP
                            }
                            context.startService(intent)
                        }
                    }
                )
            }
            
            // 悬浮窗设置组
            SettingsSection(title = "悬浮窗") {
                // 悬浮窗权限
                PermissionSettingItem(
                    title = "悬浮窗权限",
                    subtitle = if (hasOverlayPermission) "已授权" else "需要授权以显示悬浮窗",
                    icon = Icons.Default.OpenInNew,
                    isGranted = hasOverlayPermission,
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                )
                
                // 原子通知悬浮窗
                ToggleSettingItem(
                    title = "原子通知",
                    subtitle = "在屏幕上显示可拖动的实时时间追踪悬浮窗",
                    icon = Icons.Default.Widgets,
                    isEnabled = isAtomicNotificationEnabled && hasOverlayPermission,
                    onToggle = { enabled ->
                        isAtomicNotificationEnabled = enabled
                        val intent = Intent(context, AtomicNotificationService::class.java)
                        if (enabled) {
                            context.startService(intent)
                        } else {
                            context.stopService(intent)
                        }
                    }
                )
            }
            
            // 番茄钟设置组
            SettingsSection(title = "番茄钟") {
                val pomodoroNotificationEnabled = viewModel.pomodoroNotificationEnabled.collectAsState(initial = true).value
                val pomodoroCompletionAlert = viewModel.pomodoroCompletionAlert.collectAsState(initial = true).value
                
                ToggleSettingItem(
                    title = "番茄钟倒计时通知",
                    subtitle = "在通知栏显示番茄钟倒计时进度",
                    icon = Icons.Default.Timer,
                    isEnabled = pomodoroNotificationEnabled,
                    onToggle = { enabled ->
                        viewModel.setPomodoroNotificationEnabled(enabled)
                        if (enabled) {
                            Toast.makeText(context, "✓ 番茄钟倒计时通知已开启", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "✓ 番茄钟倒计时通知已关闭", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                
                ToggleSettingItem(
                    title = "番茄钟完成提醒",
                    subtitle = "番茄钟完成时弹出提醒",
                    icon = Icons.Default.Alarm,
                    isEnabled = pomodoroCompletionAlert,
                    onToggle = { enabled ->
                        viewModel.setPomodoroCompletionAlert(enabled)
                        if (enabled) {
                            Toast.makeText(context, "✓ 番茄钟完成提醒已开启", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "✓ 番茄钟完成提醒已关闭", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            
            // 提醒设置组
            SettingsSection(title = "提醒设置") {
                val blockStartReminder = viewModel.blockStartReminder.collectAsState(initial = false).value
                val blockEndReminder = viewModel.blockEndReminder.collectAsState(initial = false).value
                
                ToggleSettingItem(
                    title = "时间块开始提醒",
                    subtitle = "时间块开始时发送通知",
                    icon = Icons.Default.PlayArrow,
                    isEnabled = blockStartReminder,
                    onToggle = { enabled ->
                        viewModel.setBlockStartReminder(enabled)
                        if (enabled) {
                            Toast.makeText(context, "✓ 时间块开始提醒已开启", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "✓ 时间块开始提醒已关闭", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                
                ToggleSettingItem(
                    title = "时间块结束提醒",
                    subtitle = "时间块结束时发送通知",
                    icon = Icons.Default.Stop,
                    isEnabled = blockEndReminder,
                    onToggle = { enabled ->
                        viewModel.setBlockEndReminder(enabled)
                        if (enabled) {
                            Toast.makeText(context, "✓ 时间块结束提醒已开启", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "✓ 时间块结束提醒已关闭", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            
            // 定时提醒设置组
            SettingsSection(title = "定时提醒") {
                val intervalEnabled = viewModel.intervalReminderEnabled.collectAsState(initial = false).value
                val selectedMinutes = viewModel.intervalReminderMinutes.collectAsState(initial = SettingsViewModel.DEFAULT_REMINDER_MINUTES).value

                ToggleSettingItem(
                    title = "定时记录提醒",
                    subtitle = "在选定的时间点提醒您记录",
                    icon = Icons.Default.Timer,
                    isEnabled = intervalEnabled,
                    onToggle = { enabled ->
                        viewModel.setIntervalReminderEnabled(enabled)
                        if (enabled) {
                            IntervalReminderManager.startReminder(context)
                            Toast.makeText(context, "✓ 定时提醒已开启", Toast.LENGTH_SHORT).show()
                        } else {
                            IntervalReminderManager.stopReminder(context)
                            Toast.makeText(context, "✓ 定时提醒已关闭", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                if (intervalEnabled) {
                    // 显示当前启用的提醒时间
                    val enabledTimes = selectedMinutes.sorted().map { 
                        when (it) {
                            0 -> "整点"
                            else -> "${it}分"
                        }
                    }.joinToString("、")
                    
                    Text(
                        text = "当前提醒时间: $enabledTimes",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = iOSGreen
                        ),
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp)
                    )
                    
                    // 可勾选的提醒时间点
                    Text(
                        text = "选择提醒时间点",
                        style = MaterialTheme.typography.bodySmall,
                        color = iOSSecondaryLabel,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 15分钟
                        ReminderMinuteChip(
                            minute = 15,
                            displayText = "15分",
                            isSelected = selectedMinutes.contains(15),
                            onToggle = { 
                                val newState = !selectedMinutes.contains(15)
                                viewModel.toggleReminderMinute(15)
                                Toast.makeText(context, if (newState) "✓ 已添加 15分提醒" else "✓ 已移除 15分提醒", Toast.LENGTH_SHORT).show()
                                // 重新启动提醒以应用更改
                                if (intervalEnabled) {
                                    IntervalReminderManager.stopReminder(context)
                                    IntervalReminderManager.startReminder(context)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // 30分钟
                        ReminderMinuteChip(
                            minute = 30,
                            displayText = "30分",
                            isSelected = selectedMinutes.contains(30),
                            onToggle = { 
                                val newState = !selectedMinutes.contains(30)
                                viewModel.toggleReminderMinute(30)
                                Toast.makeText(context, if (newState) "✓ 已添加 30分提醒" else "✓ 已移除 30分提醒", Toast.LENGTH_SHORT).show()
                                if (intervalEnabled) {
                                    IntervalReminderManager.stopReminder(context)
                                    IntervalReminderManager.startReminder(context)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // 45分钟
                        ReminderMinuteChip(
                            minute = 45,
                            displayText = "45分",
                            isSelected = selectedMinutes.contains(45),
                            onToggle = { 
                                val newState = !selectedMinutes.contains(45)
                                viewModel.toggleReminderMinute(45)
                                Toast.makeText(context, if (newState) "✓ 已添加 45分提醒" else "✓ 已移除 45分提醒", Toast.LENGTH_SHORT).show()
                                if (intervalEnabled) {
                                    IntervalReminderManager.stopReminder(context)
                                    IntervalReminderManager.startReminder(context)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // 60分钟（整点）
                        ReminderMinuteChip(
                            minute = 0,
                            displayText = "整点",
                            isSelected = selectedMinutes.contains(0),
                            onToggle = { 
                                val newState = !selectedMinutes.contains(0)
                                viewModel.toggleReminderMinute(0)
                                Toast.makeText(context, if (newState) "✓ 已添加整点提醒" else "✓ 已移除整点提醒", Toast.LENGTH_SHORT).show()
                                if (intervalEnabled) {
                                    IntervalReminderManager.stopReminder(context)
                                    IntervalReminderManager.startReminder(context)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // 测试提醒按钮
                    Button(
                        onClick = {
                            // 直接显示测试通知
                            val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                            
                            val pendingIntent = android.app.PendingIntent.getActivity(
                                context,
                                9998,
                                android.content.Intent(context, com.timetracker.app.MainActivity::class.java).apply {
                                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                                },
                                android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                            )
                            
                            val notification = androidx.core.app.NotificationCompat.Builder(context, com.timetracker.app.service.notification.IntervalReminderManager.CHANNEL_ID)
                                .setContentTitle("⏰ 测试提醒")
                                .setContentText("这是一个测试通知，提醒功能正常工作！")
                                .setSmallIcon(com.timetracker.app.R.drawable.ic_notification)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                                .setVibrate(longArrayOf(0, 500, 200, 500))
                                .build()
                            
                            notificationManager.notify(9998, notification)
                            Toast.makeText(context, "✓ 测试提醒已显示", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = iOSBlue
                        )
                    ) {
                        Text("立即测试提醒")
                    }
                }
            }

            // 免打扰设置组
            SettingsSection(title = "免打扰") {
                val dndEnabled = viewModel.doNotDisturbEnabled.collectAsState(initial = false).value
                val startHour = viewModel.doNotDisturbStartHour.collectAsState(initial = 22).value
                val startMinute = viewModel.doNotDisturbStartMinute.collectAsState(initial = 0).value
                val endHour = viewModel.doNotDisturbEndHour.collectAsState(initial = 8).value
                val endMinute = viewModel.doNotDisturbEndMinute.collectAsState(initial = 0).value

                var showStartTimePicker by remember { mutableStateOf(false) }
                var showEndTimePicker by remember { mutableStateOf(false) }

                ToggleSettingItem(
                    title = "免打扰模式",
                    subtitle = "在指定时间段内不发送提醒通知",
                    icon = Icons.Default.DoNotDisturb,
                    isEnabled = dndEnabled,
                    onToggle = { viewModel.setDoNotDisturbEnabled(it) }
                )

                if (dndEnabled) {
                    // 开始时间选择
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showStartTimePicker = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(iOSBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = iOSBlue
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "开始时间",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = iOSLabel
                            )
                            Text(
                                text = String.format("%02d:%02d", startHour, startMinute),
                                style = MaterialTheme.typography.bodySmall,
                                color = iOSSecondaryLabel
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = iOSSecondaryLabel
                        )
                    }

                    Divider(modifier = Modifier.padding(horizontal = 16.dp), color = iOSGray6)

                    // 结束时间选择
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEndTimePicker = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(iOSGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = iOSGreen
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "结束时间",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = iOSLabel
                            )
                            Text(
                                text = String.format("%02d:%02d", endHour, endMinute),
                                style = MaterialTheme.typography.bodySmall,
                                color = iOSSecondaryLabel
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = iOSSecondaryLabel
                        )
                    }

                    // 时间选择器对话框
                    if (showStartTimePicker) {
                        TimePickerDialog(
                            title = "选择开始时间",
                            initialHour = startHour,
                            initialMinute = startMinute,
                            onConfirm = { hour, minute ->
                                viewModel.setDoNotDisturbStartTime(hour, minute)
                                showStartTimePicker = false
                            },
                            onDismiss = { showStartTimePicker = false }
                        )
                    }

                    if (showEndTimePicker) {
                        TimePickerDialog(
                            title = "选择结束时间",
                            initialHour = endHour,
                            initialMinute = endMinute,
                            onConfirm = { hour, minute ->
                                viewModel.setDoNotDisturbEndTime(hour, minute)
                                showEndTimePicker = false
                            },
                            onDismiss = { showEndTimePicker = false }
                        )
                    }
                }
            }

            // 电池优化设置组
            SettingsSection(title = "电池优化") {
                val isIgnoringBatteryOptimizations = remember {
                    com.timetracker.app.util.BatteryOptimizationManager.isIgnoringBatteryOptimizations(context)
                }
                
                PermissionSettingItem(
                    title = "忽略电池优化",
                    subtitle = if (isIgnoringBatteryOptimizations) "已设置 - 后台服务正常运行" else "未设置 - 建议开启以确保后台通知正常",
                    icon = Icons.Default.BatteryFull,
                    isGranted = isIgnoringBatteryOptimizations,
                    onClick = {
                        if (!isIgnoringBatteryOptimizations) {
                            com.timetracker.app.util.BatteryOptimizationManager.requestIgnoreBatteryOptimizations(
                                context as android.app.Activity
                            )
                        }
                    }
                )
                
                if (!isIgnoringBatteryOptimizations) {
                    Text(
                        text = "提示：部分手机厂商有额外的电池优化设置，建议前往系统设置中将本应用加入白名单",
                        style = MaterialTheme.typography.bodySmall,
                        color = iOSSecondaryLabel,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    Button(
                        onClick = {
                            val opened = com.timetracker.app.util.BatteryOptimizationManager.openDeviceSpecificBatterySettings(context)
                            if (!opened) {
                                com.timetracker.app.util.BatteryOptimizationManager.openBatteryOptimizationSettings(context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = iOSBlue
                        )
                    ) {
                        Text("打开系统电池设置")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 时间显示
                Text(
                    text = String.format("%02d:%02d", selectedHour, selectedMinute),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    ),
                    color = iOSBlue,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // 小时选择
                Text(
                    text = "小时",
                    style = MaterialTheme.typography.bodySmall,
                    color = iOSSecondaryLabel,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (0..23).forEach { hour ->
                        val isSelected = selectedHour == hour
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) iOSBlue else iOSGray6
                                )
                                .clickable { selectedHour = hour },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$hour",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) Color.White else iOSLabel
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 分钟选择
                Text(
                    text = "分钟",
                    style = MaterialTheme.typography.bodySmall,
                    color = iOSSecondaryLabel,
                    modifier = Modifier.align(Alignment.Start)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 15, 30, 45).forEach { minute ->
                        val isSelected = selectedMinute == minute
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) iOSBlue else iOSGray6
                                )
                                .clickable { selectedMinute = minute }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", minute),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else iOSLabel
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedHour, selectedMinute) }
            ) {
                Text("确定", color = iOSBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = iOSSecondaryLabel)
            }
        }
    )
}

@Composable
private fun ReminderMinuteChip(
    minute: Int,
    displayText: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) iOSBlue else iOSGray6
            )
            .clickable { onToggle() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 勾选标记
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = iOSBlue
                    )
                }
            }
            
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = if (isSelected) Color.White else iOSLabel
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Section title
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            color = iOSLabel,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        
        // Content card with iOS style
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(iOSSecondaryBackground)
        ) {
            content()
        }
    }
}

@Composable
private fun PermissionSettingItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (isGranted) iOSGreen.copy(alpha = 0.15f) else iOSRed.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) iOSGreen else iOSRed,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = iOSLabel
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp
                ),
                color = iOSSecondaryLabel
            )
        }
        
        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "已授权",
                tint = iOSGreen,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = "去授权",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = iOSBlue
            )
        }
    }
}

@Composable
private fun ToggleSettingItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon with background
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iOSBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iOSBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = iOSLabel
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp
                ),
                color = iOSSecondaryLabel
            )
        }
        
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = iOSGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = iOSGray6
            )
        )
    }
}

@Composable
private fun IntervalSelector(
    selectedMinutes: Int,
    onIntervalSelected: (Int) -> Unit
) {
    val intervals = listOf(15, 30, 45, 60, 90, 120)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "提醒间隔",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            color = iOSLabel,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            intervals.forEach { minutes ->
                val isSelected = selectedMinutes == minutes
                val displayText = when {
                    minutes < 60 -> "${minutes}分钟"
                    minutes == 60 -> "1小时"
                    else -> "${minutes / 60}小时"
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) iOSBlue else iOSGray6
                        )
                        .clickable { onIntervalSelected(minutes) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = if (isSelected) Color.White else iOSLabel
                    )
                }
            }
        }
    }
}
