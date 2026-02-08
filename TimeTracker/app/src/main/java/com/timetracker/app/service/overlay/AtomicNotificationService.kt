package com.timetracker.app.service.overlay

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView
import com.timetracker.app.R
import com.timetracker.app.data.local.database.TimeTrackerDatabase
import com.timetracker.app.data.local.entity.TimeBlockEntity
import com.timetracker.app.data.model.TimeNature
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@AndroidEntryPoint
class AtomicNotificationService : Service() {

    @Inject
    lateinit var database: TimeTrackerDatabase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var updateJob: Job? = null
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isExpanded = false
    private var currentAtomicView: View? = null

    companion object {
        const val ACTION_SHOW_ATOMIC = "ACTION_SHOW_ATOMIC"
        const val ACTION_HIDE_ATOMIC = "ACTION_HIDE_ATOMIC"
        const val ACTION_EXPAND = "ACTION_EXPAND"
        const val ACTION_COLLAPSE = "ACTION_COLLAPSE"
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_SHOW_ATOMIC -> showAtomicNotification()
                ACTION_HIDE_ATOMIC -> hideAtomicNotification()
                ACTION_EXPAND -> expandOverlay()
                ACTION_COLLAPSE -> collapseOverlay()
                "POMODORO_TICK" -> updatePomodoroDisplay(
                    intent.getIntExtra("remaining", 0),
                    intent.getBooleanExtra("isBreak", false)
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        // 检查悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        val filter = IntentFilter().apply {
            addAction(ACTION_SHOW_ATOMIC)
            addAction(ACTION_HIDE_ATOMIC)
            addAction(ACTION_EXPAND)
            addAction(ACTION_COLLAPSE)
            addAction("POMODORO_TICK")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(broadcastReceiver, filter)
        }
        
        createOverlayView()
        startPeriodicUpdate()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    private fun createOverlayView() {
        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = 32
                y = 200
            }

            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_atomic_notification, null)
            
            setupTouchListener()
            
            overlayView?.findViewById<View>(R.id.container_header)?.setOnClickListener {
                if (isExpanded) {
                    collapseOverlay()
                } else {
                    expandOverlay()
                }
            }

            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun setupTouchListener() {
        overlayView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            private var isDragging = false
            private val touchSlop = 10

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = (overlayView?.layoutParams as? WindowManager.LayoutParams)?.x ?: 0
                        initialY = (overlayView?.layoutParams as? WindowManager.LayoutParams)?.y ?: 0
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isDragging = false
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = (event.rawX - initialTouchX).toInt()
                        val deltaY = (event.rawY - initialTouchY).toInt()
                        
                        if (kotlin.math.abs(deltaX) > touchSlop || kotlin.math.abs(deltaY) > touchSlop) {
                            isDragging = true
                            val params = overlayView?.layoutParams as? WindowManager.LayoutParams
                            params?.x = initialX - deltaX
                            params?.y = initialY + deltaY
                            try {
                                windowManager?.updateViewLayout(overlayView, params)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            v?.performClick()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                try {
                    updateOverlayContent()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(1000)
            }
        }
    }

    private suspend fun updateOverlayContent() {
        val today = LocalDate.now()
        val todayMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val tomorrowMillis = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val timeBlocks = database.timeBlockDao().getTimeBlocksForWidget(todayMillis, tomorrowMillis)
        val currentTimeMillis = System.currentTimeMillis()

        val currentBlock = timeBlocks.find { block ->
            block.startTime <= currentTimeMillis && block.endTime > currentTimeMillis
        }

        overlayView?.let { view ->
            val tvCurrentTitle = view.findViewById<TextView>(R.id.tv_current_title)
            val tvCurrentTime = view.findViewById<TextView>(R.id.tv_current_time)
            val tvRemaining = view.findViewById<TextView>(R.id.tv_remaining)
            val indicator = view.findViewById<View>(R.id.indicator_current)

            if (currentBlock != null) {
                tvCurrentTitle?.text = currentBlock.title
                tvCurrentTime?.text = formatTimeRange(currentBlock)
                tvRemaining?.text = calculateRemaining(currentBlock)
                
                try {
                    val color = android.graphics.Color.parseColor(currentBlock.color)
                    indicator?.setBackgroundColor(color)
                } catch (e: Exception) {
                    indicator?.setBackgroundColor(android.graphics.Color.parseColor("#007AFF"))
                }
            } else {
                tvCurrentTitle?.text = "暂无进行中的时间块"
                tvCurrentTime?.text = "点击添加"
                tvRemaining?.text = ""
                indicator?.setBackgroundColor(android.graphics.Color.parseColor("#CCCCCC"))
            }

            updateDailyStats(view, timeBlocks)
        }
    }

    private fun updateDailyStats(view: View, blocks: List<TimeBlockEntity>) {
        var productiveMinutes = 0
        var unproductiveMinutes = 0
        var neutralMinutes = 0

        blocks.forEach { block ->
            val durationMinutes = ((block.endTime - block.startTime) / (1000 * 60)).toInt()
            val nature = try {
                TimeNature.valueOf(block.timeNature)
            } catch (e: Exception) {
                TimeNature.PRODUCTIVE
            }
            when (nature) {
                TimeNature.PRODUCTIVE -> productiveMinutes += durationMinutes
                TimeNature.UNPRODUCTIVE -> unproductiveMinutes += durationMinutes
                TimeNature.NEUTRAL -> neutralMinutes += durationMinutes
            }
        }

        val totalMinutes = productiveMinutes + unproductiveMinutes + neutralMinutes
        val efficiency = if (totalMinutes > 0) {
            (productiveMinutes.toFloat() / totalMinutes * 100).toInt()
        } else 0

        view.findViewById<TextView>(R.id.tv_productive)?.text = "元气满满: ${productiveMinutes}分钟"
        view.findViewById<TextView>(R.id.tv_unproductive)?.text = "摸鱼时光: ${unproductiveMinutes}分钟"
        view.findViewById<TextView>(R.id.tv_efficiency)?.text = "效率: ${efficiency}%"
    }

    private fun updatePomodoroDisplay(remainingSeconds: Int, isBreak: Boolean) {
        overlayView?.let { view ->
            val tvPomodoro = view.findViewById<TextView>(R.id.tv_pomodoro)
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            val timeText = String.format("%02d:%02d", minutes, seconds)
            
            tvPomodoro?.text = if (isBreak) "☕ 休息 $timeText" else "🍅 专注 $timeText"
            tvPomodoro?.visibility = View.VISIBLE
        }
    }

    private fun showAtomicNotification() {
        // 先隐藏之前的
        hideAtomicNotification()

        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_TOAST
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
            }

            val atomicView = LayoutInflater.from(this).inflate(R.layout.overlay_atomic_toast, null)
            
            val fadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 300
                fillAfter = true
            }
            atomicView.startAnimation(fadeIn)

            windowManager?.addView(atomicView, params)
            currentAtomicView = atomicView

            serviceScope.launch {
                delay(3000)
                hideAtomicNotification()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideAtomicNotification() {
        currentAtomicView?.let { view ->
            val fadeOut = AlphaAnimation(1f, 0f).apply {
                duration = 300
                fillAfter = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        try {
                            windowManager?.removeView(view)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
            view.startAnimation(fadeOut)
        }
        currentAtomicView = null
    }

    private fun expandOverlay() {
        overlayView?.let { view ->
            val contentExpanded = view.findViewById<View>(R.id.content_expanded)
            contentExpanded?.visibility = View.VISIBLE
            
            val fadeIn = AlphaAnimation(0f, 1f).apply {
                duration = 200
                fillAfter = true
            }
            contentExpanded?.startAnimation(fadeIn)
            
            isExpanded = true
        }
    }

    private fun collapseOverlay() {
        overlayView?.let { view ->
            val contentExpanded = view.findViewById<View>(R.id.content_expanded)
            
            val fadeOut = AlphaAnimation(1f, 0f).apply {
                duration = 200
                fillAfter = true
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        contentExpanded?.visibility = View.GONE
                    }
                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            }
            contentExpanded?.startAnimation(fadeOut)
            
            isExpanded = false
        }
    }

    private fun formatTimeRange(block: TimeBlockEntity): String {
        val start = java.time.Instant.ofEpochMilli(block.startTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        val end = java.time.Instant.ofEpochMilli(block.endTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        return String.format("%02d:%02d-%02d:%02d", start.hour, start.minute, end.hour, end.minute)
    }

    private fun calculateRemaining(block: TimeBlockEntity): String {
        val now = System.currentTimeMillis()
        val remainingMs = block.endTime - now
        if (remainingMs <= 0) return "即将结束"
        val remainingMinutes = (remainingMs / (1000 * 60)).toInt()
        return "剩余${remainingMinutes}分钟"
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        updateJob?.cancel()
        serviceScope.cancel()
        
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        currentAtomicView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
