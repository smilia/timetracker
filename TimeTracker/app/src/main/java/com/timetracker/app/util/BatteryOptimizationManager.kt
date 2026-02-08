package com.timetracker.app.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.getSystemService

/**
 * 电池优化管理器
 * 用于检查和请求忽略电池优化，确保后台服务正常运行
 */
object BatteryOptimizationManager {

    /**
     * 检查是否已忽略电池优化
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService<PowerManager>()
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
        } else {
            true
        }
    }

    /**
     * 请求忽略电池优化
     * 需要在 Activity 中调用
     */
    fun requestIgnoreBatteryOptimizations(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        }
    }

    /**
     * 打开电池优化设置页面
     */
    fun openBatteryOptimizationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }
        context.startActivity(intent)
    }

    /**
     * 获取设备厂商的电池优化页面（针对特定厂商的深度优化）
     */
    fun getDeviceSpecificBatteryIntent(context: Context): Intent? {
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        return when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> {
                // 小米/红米
                Intent().apply {
                    component = android.content.ComponentName(
                        "com.miui.powerkeeper",
                        "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"
                    )
                }
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                // 华为/荣耀
                Intent().apply {
                    component = android.content.ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity"
                    )
                }
            }
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> {
                // OPPO/Realme
                Intent().apply {
                    component = android.content.ComponentName(
                        "com.coloros.oppoguardelf",
                        "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"
                    )
                }
            }
            manufacturer.contains("vivo") -> {
                // VIVO
                Intent().apply {
                    component = android.content.ComponentName(
                        "com.vivo.abe",
                        "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity"
                    )
                }
            }
            manufacturer.contains("samsung") -> {
                // 三星
                Intent().apply {
                    component = android.content.ComponentName(
                        "com.samsung.android.sm",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"
                    )
                }
            }
            else -> null
        }
    }

    /**
     * 尝试打开厂商特定的电池优化页面
     */
    fun openDeviceSpecificBatterySettings(context: Context): Boolean {
        val intent = getDeviceSpecificBatteryIntent(context)
        return if (intent != null) {
            try {
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
}
