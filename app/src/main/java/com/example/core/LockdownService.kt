package com.example.core

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LockdownService : Service() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var usage: UsageStatsManager? = null
    private var lastCheck = 0L
    private var escapes = 0
    private var polling = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        usage = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1101, buildNotification())
        if (!polling) {
            polling = true
            lastCheck = System.currentTimeMillis()
            scope.launch { poll() }
        }
        return START_STICKY
    }

    private suspend fun poll() {
        while (true) {
            delay(1000L)
            if (!LockdownManager.isEnabled(this)) continue
            val now = System.currentTimeMillis()
            val events = usage?.queryEvents(lastCheck, now) ?: continue
            lastCheck = now
            var foreground: String? = null
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foreground = event.packageName
                }
            }
            val pkg = foreground ?: continue
            if (pkg == packageName) continue
            if (LockdownManager.blockedPackages(this).contains(pkg)) {
                onBusted(pkg)
            }
        }
    }

    private fun onBusted(pkg: String) {
        escapes++
        val penalty = 3 + escapes * 2
        scope.launch {
            try {
                AppCore.repository.addShame(penalty, "Lockdown breach: opened a blocked app")
            } catch (_: UninitializedPropertyAccessException) { }
        }
        val name = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (_: Exception) { pkg }
        LockdownBustedState.lastApp = name
        LockdownBustedState.lastPenalty = penalty
        LockdownBustedState.pendingFlow.value = true
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
        } catch (_: Exception) { }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, NotificationHelper.CHANNEL_SAVAGE)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("LOCKDOWN ACTIVE")
            .setContentText("Distracting apps are sealed until your session ends.")
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
