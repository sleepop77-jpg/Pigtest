package com.example.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.core.WidgetBridge
import com.example.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

object StudyWidgets {
    fun refreshAnimated(ctx: Context) {
        CompanionWidgetProvider.refresh(ctx)
        DangerWidgetProvider.refresh(ctx)
        StreakWidgetProvider.refresh(ctx)
        FocusRingWidgetProvider.refresh(ctx)
    }

    fun refreshData(ctx: Context) {
        TickerWidgetProvider.refresh(ctx)
        QuestWidgetProvider.refresh(ctx)
        FocusRingWidgetProvider.refresh(ctx)
    }

    fun pi(ctx: Context, route: String?): PendingIntent {
        val i = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (route != null) putExtra("studyos_route", route)
        }
        return PendingIntent.getActivity(ctx, route?.hashCode() ?: 100, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun size(ctx: Context, wDp: Int, hDp: Int): Pair<Int, Int> {
        val d = ctx.resources.displayMetrics.density
        return (wDp * d).toInt() to (hDp * d).toInt()
    }

    fun set(ctx: Context, mgr: AppWidgetManager, id: Int, bmp: android.graphics.Bitmap, route: String?) {
        val views = RemoteViews(ctx.packageName, R.layout.widget_image)
        views.setImageViewBitmap(R.id.widget_img, bmp)
        views.setOnClickPendingIntent(R.id.widget_img, pi(ctx, route))
        mgr.updateAppWidget(id, views)
    }
}

class CompanionWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) { ids.forEach { draw(ctx, mgr, it) } }
    companion object {
        fun draw(ctx: Context, mgr: AppWidgetManager, id: Int) {
            val (w, h) = StudyWidgets.size(ctx, 300, 150)
            val bmp = WidgetArt.renderMascot(ctx, w, h, WidgetBridge.frame.value, WidgetBridge.streak.value, "Keep going - you are building something real.")
            StudyWidgets.set(ctx, mgr, id, bmp, null)
        }
        fun refresh(ctx: Context) { val mgr = AppWidgetManager.getInstance(ctx); mgr.getAppWidgetIds(ComponentName(ctx, CompanionWidgetProvider::class.java)).forEach { draw(ctx, mgr, it) } }
    }
}

class TickerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) { ids.forEach { draw(ctx, mgr, it) } }
    companion object {
        fun draw(ctx: Context, mgr: AppWidgetManager, id: Int) {
            val (w, h) = StudyWidgets.size(ctx, 300, 150)
            val data = try {
                runBlocking { AppDatabase.getDatabase(ctx).stockDao().getAllStocks().first() }
                    .take(2).map { it.symbol to 2.4f }
            } catch (_: Exception) { listOf("\$MATH" to 2.4f, "\$CS" to -1.2f) }
            val bmp = WidgetArt.renderTicker(ctx, w, h, data)
            StudyWidgets.set(ctx, mgr, id, bmp, "stocks")
        }
        fun refresh(ctx: Context) { val mgr = AppWidgetManager.getInstance(ctx); mgr.getAppWidgetIds(ComponentName(ctx, TickerWidgetProvider::class.java)).forEach { draw(ctx, mgr, it) } }
    }
}

class DangerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) { ids.forEach { draw(ctx, mgr, it) } }
    companion object {
        fun draw(ctx: Context, mgr: AppWidgetManager, id: Int) {
            val (w, h) = StudyWidgets.size(ctx, 150, 150)
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val label = if (hour in 16 until 18) {
                "ACTIVE!"
            } else {
                val target = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 16); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); if (before(cal)) add(Calendar.DAY_OF_YEAR, 1) }
                val mins = ((target.timeInMillis - cal.timeInMillis) / 60000).toInt()
                "${mins / 60}h ${mins % 60}m"
            }
            val bmp = WidgetArt.renderDanger(ctx, w, h, WidgetBridge.frame.value, label)
            StudyWidgets.set(ctx, mgr, id, bmp, "pomodoro")
        }
        fun refresh(ctx: Context) { val mgr = AppWidgetManager.getInstance(ctx); mgr.getAppWidgetIds(ComponentName(ctx, DangerWidgetProvider::class.java)).forEach { draw(ctx, mgr, it) } }
    }
}

class FocusRingWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) { ids.forEach { draw(ctx, mgr, it) } }
    companion object {
        fun draw(ctx: Context, mgr: AppWidgetManager, id: Int) {
            val (w, h) = StudyWidgets.size(ctx, 300, 150)
            val bmp = WidgetArt.renderFocus(ctx, w, h, WidgetBridge.seconds.value, 25 * 60, WidgetBridge.running.value, WidgetBridge.frame.value)
            StudyWidgets.set(ctx, mgr, id, bmp, "pomodoro")
        }
        fun refresh(ctx: Context) { val mgr = AppWidgetManager.getInstance(ctx); mgr.getAppWidgetIds(ComponentName(ctx, FocusRingWidgetProvider::class.java)).forEach { draw(ctx, mgr, it) } }
    }
}

class StreakWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) { ids.forEach { draw(ctx, mgr, it) } }
    companion object {
        fun draw(ctx: Context, mgr: AppWidgetManager, id: Int) {
            val (w, h) = StudyWidgets.size(ctx, 150, 150)
            val bmp = WidgetArt.renderStreak(ctx, w, h, WidgetBridge.frame.value, WidgetBridge.streak.value)
            StudyWidgets.set(ctx, mgr, id, bmp, null)
        }
        fun refresh(ctx: Context) { val mgr = AppWidgetManager.getInstance(ctx); mgr.getAppWidgetIds(ComponentName(ctx, StreakWidgetProvider::class.java)).forEach { draw(ctx, mgr, it) } }
    }
}

class QuestWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) { ids.forEach { draw(ctx, mgr, it) } }
    companion object {
        fun draw(ctx: Context, mgr: AppWidgetManager, id: Int) {
            val (w, h) = StudyWidgets.size(ctx, 300, 150)
            val quests = try {
                runBlocking { AppDatabase.getDatabase(ctx).studyGoalDao().getAllGoals().first() }
                    .take(3).map { Triple(it.title, it.currentValue, it.targetValue) }
            } catch (_: Exception) { listOf(Triple("Set your first goal", 0, 1)) }
            val bmp = WidgetArt.renderQuests(ctx, w, h, quests)
            StudyWidgets.set(ctx, mgr, id, bmp, "tasks_goals")
        }
        fun refresh(ctx: Context) { val mgr = AppWidgetManager.getInstance(ctx); mgr.getAppWidgetIds(ComponentName(ctx, QuestWidgetProvider::class.java)).forEach { draw(ctx, mgr, it) } }
    }
}
