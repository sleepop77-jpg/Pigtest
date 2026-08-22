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
import com.example.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class StatsWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { update(context, appWidgetManager, it) }
    }

    companion object {
        private var lastFame = -1

        fun update(context: Context, mgr: AppWidgetManager, id: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_stats)
            val db = AppDatabase.getDatabase(context)
            val fame = runBlocking { db.economyDao().getTotalFame().first() }
            val shame = runBlocking { db.economyDao().getTotalShame().first() }
            views.setTextViewText(R.id.widget_fame, fame.toString())
            views.setTextViewText(R.id.widget_shame, shame.toString())
            val openApp = PendingIntent.getActivity(
                context, 2001,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_stats_root, openApp)
            mgr.updateAppWidget(id, views)
        }

        fun refreshAll(context: Context) {
            val db = AppDatabase.getDatabase(context)
            val fame = runBlocking { db.economyDao().getTotalFame().first() }
            if (fame == lastFame) return
            lastFame = fame
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, StatsWidgetProvider::class.java))
            ids.forEach { update(context, mgr, it) }
        }
    }
}
