package com.example.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.WidgetBridge

class FocusWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { update(context, appWidgetManager, it) }
    }

    companion object {
        private var lastText = ""

        fun update(context: Context, mgr: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_focus)
            val secs = WidgetBridge.seconds.value
            val running = WidgetBridge.running.value
            views.setTextViewText(R.id.widget_timer, String.format("%02d:%02d", secs / 60, secs % 60))
            views.setTextViewText(R.id.widget_timer_state, if (running) "LOCKED IN" else "IDLE")
            views.setTextViewText(R.id.widget_focus_btn, if (running) "RETURN TO TIMER" else "START FOCUS")
            views.setInt(R.id.widget_flame, "setColorFilter", 0xFFFFD700.toInt())
            val openTimer = PendingIntent.getActivity(
                context, 2002,
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("studyos_route", "pomodoro")
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_focus_btn, openTimer)
            views.setOnClickPendingIntent(R.id.widget_focus_root, openTimer)
            mgr.updateAppWidget(id, views)
        }

        fun refreshAll(context: Context) {
            val secs = WidgetBridge.seconds.value
            val text = String.format("%02d:%02d", secs / 60, secs % 60) + WidgetBridge.running.value
            if (text == lastText) return
            lastText = text
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, FocusWidgetProvider::class.java))
            ids.forEach { update(context, mgr, it) }
        }
    }
}
