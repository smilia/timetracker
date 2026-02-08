package com.timetracker.app.data.model

enum class TimeNature {
    PRODUCTIVE,     // 元气满满 - 高效时间
    UNPRODUCTIVE,   // 摸鱼时光 - 低效时间
    NEUTRAL         // 中性 - 不计入效率统计
}

fun TimeNature.toDisplayName(): String {
    return when (this) {
        TimeNature.PRODUCTIVE -> "元气满满"
        TimeNature.UNPRODUCTIVE -> "摸鱼时光"
        TimeNature.NEUTRAL -> "中性"
    }
}

fun TimeNature.toColor(): String {
    return when (this) {
        TimeNature.PRODUCTIVE -> "#81C784"  // 浅绿色
        TimeNature.UNPRODUCTIVE -> "#FF8A65" // 浅红色
        TimeNature.NEUTRAL -> "#90A4AE"      // 灰色
    }
}
