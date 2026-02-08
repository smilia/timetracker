package com.timetracker.app.ui.screens.templates

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.timetracker.app.data.model.Template
import com.timetracker.app.data.model.TimeNature
import com.timetracker.app.ui.theme.iOSBlue
import com.timetracker.app.ui.theme.iOSGray3
import com.timetracker.app.ui.theme.iOSGray5
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
fun TemplatesScreen(
    viewModel: TemplatesViewModel = hiltViewModel()
) {
    val templates by viewModel.templates.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTemplate by remember { mutableStateOf<Template?>(null) }
    var showLabelSettings by remember { mutableStateOf(false) }
    
    // Expand/collapse states (default: expanded)
    var isTemplatesExpanded by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "时间块模板",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = iOSLabel
                    )
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "添加模板", tint = iOSBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = iOSSystemBackground,
                    titleContentColor = iOSLabel,
                    actionIconContentColor = iOSBlue
                )
            )
        },
        containerColor = iOSSystemBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Label settings card
            item {
                iOSLabelSettingsCard(
                    productiveLabel = viewModel.productiveLabel,
                    unproductiveLabel = viewModel.unproductiveLabel,
                    onClick = { showLabelSettings = true }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // All templates section with expand/collapse
            item {
                ExpandableSectionHeader(
                    title = "所有模板",
                    count = templates.size,
                    isExpanded = isTemplatesExpanded,
                    onToggle = { isTemplatesExpanded = !isTemplatesExpanded }
                )
            }

            // Show templates only when expanded
            if (isTemplatesExpanded) {
                items(templates) { template ->
                    iOSTemplateCard(
                        template = template,
                        productiveLabel = viewModel.productiveLabel,
                        unproductiveLabel = viewModel.unproductiveLabel,
                        onEdit = { editingTemplate = template },
                        onToggleFrequent = { viewModel.toggleFrequent(template) },
                        onDelete = { viewModel.deleteTemplate(template) }
                    )
                }
            }
        }
    }

    // Add/Edit Template Dialog
    if (showAddDialog || editingTemplate != null) {
        iOSTemplateDialog(
            template = editingTemplate,
            productiveLabel = viewModel.productiveLabel,
            unproductiveLabel = viewModel.unproductiveLabel,
            onDismiss = {
                showAddDialog = false
                editingTemplate = null
            },
            onConfirm = { name, color, duration, isFrequent, timeNature ->
                if (editingTemplate != null) {
                    viewModel.updateTemplate(
                        editingTemplate!!.copy(
                            name = name,
                            color = color,
                            defaultDuration = duration,
                            isFrequent = isFrequent,
                            timeNature = timeNature
                        )
                    )
                } else {
                    viewModel.addTemplate(name, color, duration, isFrequent, timeNature)
                }
                showAddDialog = false
                editingTemplate = null
            }
        )
    }

    // Label Settings Dialog
    if (showLabelSettings) {
        iOSLabelSettingsDialog(
            productiveLabel = viewModel.productiveLabel,
            unproductiveLabel = viewModel.unproductiveLabel,
            onDismiss = { showLabelSettings = false },
            onConfirm = { productive, unproductive ->
                viewModel.setProductiveLabel(productive)
                viewModel.setUnproductiveLabel(unproductive)
                showLabelSettings = false
            }
        )
    }
}

@Composable
private fun ExpandableSectionHeader(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = iOSLabel
            )
            // Count badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(iOSGray5)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = iOSSecondaryLabel
                )
            }
        }

        // Expand/collapse icon
        val rotation by animateFloatAsState(
            targetValue = if (isExpanded) 180f else 0f,
            label = "rotation"
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "收起" else "展开",
            tint = iOSBlue,
            modifier = Modifier
                .size(28.dp)
                .rotate(rotation)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun iOSTemplateCard(
    template: Template,
    productiveLabel: String,
    unproductiveLabel: String,
    onEdit: () -> Unit,
    onToggleFrequent: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(template.color))
    } catch (e: Exception) {
        iOSBlue
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit,
        colors = CardDefaults.cardColors(
            containerColor = iOSSecondaryBackground
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator - iOS style
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Template info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = iOSLabel
                )
                Text(
                    text = "${template.defaultDuration}分钟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = iOSSecondaryLabel
                )
                // Time nature indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val natureColor = when (template.timeNature) {
                        TimeNature.PRODUCTIVE -> iOSGreen
                        TimeNature.UNPRODUCTIVE -> iOSOrange
                        TimeNature.NEUTRAL -> iOSGray3
                    }
                    val natureLabel = when (template.timeNature) {
                        TimeNature.PRODUCTIVE -> productiveLabel
                        TimeNature.UNPRODUCTIVE -> unproductiveLabel
                        TimeNature.NEUTRAL -> "中性"
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(natureColor)
                    )
                    Text(
                        text = natureLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = natureColor
                    )
                }
            }

            // Actions
            IconButton(onClick = onToggleFrequent) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "设为常用",
                    tint = if (template.isFrequent) {
                        iOSOrange
                    } else {
                        iOSGray3
                    }
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = iOSRed
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun iOSTemplateDialog(
    template: Template?,
    productiveLabel: String,
    unproductiveLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Boolean, TimeNature) -> Unit
) {
    var name by remember { mutableStateOf(template?.name ?: "") }
    var selectedColor by remember { mutableStateOf(template?.color ?: "#5B9BD5") }
    var duration by remember { mutableStateOf(template?.defaultDuration?.toString() ?: "15") }
    var isFrequent by remember { mutableStateOf(template?.isFrequent ?: false) }
    var timeNature by remember { mutableStateOf(template?.timeNature ?: TimeNature.PRODUCTIVE) }
    var durationError by remember { mutableStateOf(false) }
    var showCustomColorPicker by remember { mutableStateOf(false) }

    // Predefined color palette
    val colorOptions = listOf(
        "#5B9BD5", // Blue
        "#70AD47", // Green
        "#ED7D31", // Orange
        "#E85D75", // Pink
        "#9F6DD3", // Purple
        "#4DB3D8", // Cyan
        "#E15759", // Red
        "#A5A5A5", // Gray
        "#FFC000", // Yellow
        "#4472C4", // Dark Blue
        "#2E7D32", // Dark Green
        "#C55A11", // Dark Orange
        "#8E24AA", // Deep Purple
        "#00ACC1", // Teal
        "#D32F2F", // Dark Red
        "#F57C00"  // Amber
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (template == null) "添加模板" else "编辑模板",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = iOSLabel
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Name - iOS style
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("模板名称") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSBlue,
                        focusedLabelColor = iOSBlue
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Color selection - iOS style
                Text(
                    text = "选择颜色",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = iOSLabel
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Color palette grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.chunked(4).forEach { rowColors ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowColors.forEach { colorHex ->
                                val isSelected = selectedColor == colorHex
                                val color = try {
                                    Color(android.graphics.Color.parseColor(colorHex))
                                } catch (e: Exception) {
                                    iOSBlue
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) iOSLabel else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedColor = colorHex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                            if (rowColors.size < 4) {
                                repeat(4 - rowColors.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom color button
                TextButton(
                    onClick = { showCustomColorPicker = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("自定义颜色", color = iOSBlue)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Duration - iOS style
                OutlinedTextField(
                    value = duration,
                    onValueChange = {
                        duration = it
                        durationError = it.toIntOrNull()?.let { it < 15 } ?: true
                    },
                    label = { Text("默认时长（分钟）") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = durationError,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSBlue,
                        focusedLabelColor = iOSBlue,
                        errorBorderColor = iOSRed
                    ),
                    supportingText = {
                        if (durationError) {
                            Text("最少15分钟", color = iOSRed)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Is frequent - iOS style switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "设为常用模板",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = iOSLabel
                        )
                        Text(
                            text = "在日历侧边栏快速访问",
                            style = MaterialTheme.typography.bodySmall,
                            color = iOSSecondaryLabel
                        )
                    }
                    Switch(
                        checked = isFrequent,
                        onCheckedChange = { isFrequent = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = iOSBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = iOSGray3
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time nature - iOS style
                Text(
                    text = "时间性质",
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
                        selected = timeNature == TimeNature.PRODUCTIVE,
                        onClick = { timeNature = TimeNature.PRODUCTIVE },
                        label = productiveLabel,
                        color = iOSGreen,
                        modifier = Modifier.weight(1f)
                    )

                    iOSFilterChip(
                        selected = timeNature == TimeNature.UNPRODUCTIVE,
                        onClick = { timeNature = TimeNature.UNPRODUCTIVE },
                        label = unproductiveLabel,
                        color = iOSOrange,
                        modifier = Modifier.weight(1f)
                    )

                    iOSFilterChip(
                        selected = timeNature == TimeNature.NEUTRAL,
                        onClick = { timeNature = TimeNature.NEUTRAL },
                        label = "中性",
                        color = iOSGray3,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val durationValue = duration.toIntOrNull() ?: 15
                    onConfirm(name, selectedColor, durationValue, isFrequent, timeNature)
                },
                enabled = name.isNotBlank() &&
                         !durationError &&
                         (duration.toIntOrNull() ?: 0) >= 15,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iOSBlue,
                    disabledContainerColor = iOSGray3
                )
            ) {
                Text("确定", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        containerColor = iOSSystemBackground,
        shape = RoundedCornerShape(20.dp)
    )

    // Custom color picker dialog
    if (showCustomColorPicker) {
        CustomColorPickerDialog(
            initialColor = selectedColor,
            onDismiss = { showCustomColorPicker = false },
            onConfirm = { color ->
                selectedColor = color
                showCustomColorPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomColorPickerDialog(
    initialColor: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var hexColor by remember { mutableStateOf(initialColor.replace("#", "")) }
    var red by remember { mutableStateOf(91) }
    var green by remember { mutableStateOf(155) }
    var blue by remember { mutableStateOf(213) }

    // Parse initial color
    LaunchedEffect(initialColor) {
        try {
            val color = Color(android.graphics.Color.parseColor(initialColor))
            red = (color.red * 255).toInt()
            green = (color.green * 255).toInt()
            blue = (color.blue * 255).toInt()
        } catch (e: Exception) {
            // Use default values
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "自定义颜色",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = iOSLabel
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Color preview
                val currentColor = Color(red / 255f, green / 255f, blue / 255f)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(currentColor)
                        .border(2.dp, iOSGray3, RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // RGB Sliders
                Text(
                    text = "红色: $red",
                    style = MaterialTheme.typography.bodyMedium,
                    color = iOSLabel
                )
                Slider(
                    value = red.toFloat(),
                    onValueChange = { red = it.toInt() },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Red,
                        activeTrackColor = Color.Red
                    )
                )

                Text(
                    text = "绿色: $green",
                    style = MaterialTheme.typography.bodyMedium,
                    color = iOSLabel
                )
                Slider(
                    value = green.toFloat(),
                    onValueChange = { green = it.toInt() },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Green,
                        activeTrackColor = Color.Green
                    )
                )

                Text(
                    text = "蓝色: $blue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = iOSLabel
                )
                Slider(
                    value = blue.toFloat(),
                    onValueChange = { blue = it.toInt() },
                    valueRange = 0f..255f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Blue,
                        activeTrackColor = Color.Blue
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hex input
                OutlinedTextField(
                    value = hexColor,
                    onValueChange = { 
                        hexColor = it.take(6)
                        // Try to parse hex and update RGB
                        try {
                            val colorInt = android.graphics.Color.parseColor("#$hexColor")
                            red = android.graphics.Color.red(colorInt)
                            green = android.graphics.Color.green(colorInt)
                            blue = android.graphics.Color.blue(colorInt)
                        } catch (e: Exception) {
                            // Invalid hex, ignore
                        }
                    },
                    label = { Text("HEX 颜色代码") },
                    prefix = { Text("#") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hex = String.format("#%02X%02X%02X", red, green, blue)
                    onConfirm(hex)
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = iOSBlue)
            ) {
                Text("确定", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        containerColor = iOSSystemBackground,
        shape = RoundedCornerShape(20.dp)
    )
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
                if (selected) color.copy(alpha = 0.15f) else iOSSystemBackground
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) color else iOSGray3,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (selected) color else iOSLabel,
                fontSize = 13.sp
            )
        }
    }
}

// Label Settings Card
@Composable
private fun iOSLabelSettingsCard(
    productiveLabel: String,
    unproductiveLabel: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = iOSSecondaryBackground
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "时间性质标签",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = iOSLabel
                )
                Text(
                    text = "$productiveLabel / $unproductiveLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = iOSSecondaryLabel
                )
            }
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "编辑",
                tint = iOSBlue
            )
        }
    }
}

// Label Settings Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun iOSLabelSettingsDialog(
    productiveLabel: String,
    unproductiveLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var productive by remember { mutableStateOf(productiveLabel) }
    var unproductive by remember { mutableStateOf(unproductiveLabel) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "设置时间性质标签",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = iOSLabel
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = productive,
                    onValueChange = { productive = it },
                    label = { Text("非堕落标签") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSGreen,
                        focusedLabelColor = iOSGreen
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = unproductive,
                    onValueChange = { unproductive = it },
                    label = { Text("堕落标签") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = iOSOrange,
                        focusedLabelColor = iOSOrange
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (productive.isNotBlank() && unproductive.isNotBlank()) {
                        onConfirm(productive, unproductive)
                    }
                },
                enabled = productive.isNotBlank() && unproductive.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iOSBlue,
                    disabledContainerColor = iOSGray3
                )
            ) {
                Text("确定", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        containerColor = iOSSystemBackground,
        shape = RoundedCornerShape(20.dp)
    )
}
