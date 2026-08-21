package com.example.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_POMODORO = "studyos_pomodoro_channel"
    const val CHANNEL_SAVAGE = "studyos_savage_channel"
    const val CHANNEL_GOALS = "studyos_goals_channel"

    private const val NOTIF_ID_POMODORO = 1001
    private const val NOTIF_ID_SAVAGE = 1002
    private const val NOTIF_ID_GOAL = 1003

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val pomodoroChannel = NotificationChannel(
                CHANNEL_POMODORO,
                "Study Timer Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when Focus Timer rounds conclude"
                enableVibration(true)
            }
            val savageChannel = NotificationChannel(
                CHANNEL_SAVAGE,
                "Circadian Focus & Shame Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Humorous and sarcastic anti-procrastination notifications"
                enableVibration(true)
            }
            val goalsChannel = NotificationChannel(
                CHANNEL_GOALS,
                "Study Goals & Market Milestones",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Goal progress, stock market payouts, and Fame rewards"
            }
            notificationManager.createNotificationChannel(pomodoroChannel)
            notificationManager.createNotificationChannel(savageChannel)
            notificationManager.createNotificationChannel(goalsChannel)
        }
    }

    private fun buildCustomNotification(
        context: Context,
        channelId: String,
        notifId: Int,
        title: String,
        message: String,
        accentColor: Int
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val collapsed = RemoteViews(context.packageName, R.layout.notif_collapsed)
        collapsed.setTextViewText(R.id.notif_title, title)
        collapsed.setTextViewText(R.id.notif_text, message)
        collapsed.setInt(R.id.notif_icon, "setColorFilter", accentColor)

        val expanded = RemoteViews(context.packageName, R.layout.notif_expanded)
        expanded.setTextViewText(R.id.notif_title_big, title)
        expanded.setTextViewText(R.id.notif_text_big, message)
        expanded.setInt(R.id.notif_icon_big, "setColorFilter", accentColor)
        expanded.setOnClickPendingIntent(R.id.notif_action_btn, pendingIntent)

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notif_icon_flame)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(collapsed)
            .setCustomBigContentView(expanded)
            .setCustomHeadsUpContentView(collapsed)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(accentColor)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun sendPomodoroFinished(context: Context, subject: String, minutes: Int, fameEarned: Int) {
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIF_ID_POMODORO,
                buildCustomNotification(
                    context, CHANNEL_POMODORO, NOTIF_ID_POMODORO,
                    "Focus Session Complete! (+$fameEarned Fame)",
                    "You crushed $minutes min of deep focus in $subject. Your stock is rising!",
                    0xFFFFD700.toInt()
                )
            )
        } catch (_: SecurityException) { }
    }

    fun sendBreakFinished(context: Context, nextRound: Int) {
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIF_ID_POMODORO,
                buildCustomNotification(
                    context, CHANNEL_POMODORO, NOTIF_ID_POMODORO,
                    "Break is Over! (Round $nextRound/4)",
                    "Time to lock back in. Your study buddy is waiting at the desk!",
                    0xFFD9534F.toInt()
                )
            )
        } catch (_: SecurityException) { }
    }

    fun sendSavageAlert(context: Context, title: String, message: String) {
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIF_ID_SAVAGE,
                buildCustomNotification(
                    context, CHANNEL_SAVAGE, NOTIF_ID_SAVAGE,
                    title,
                    message,
                    0xFFC41C3B.toInt()
                )
            )
        } catch (_: SecurityException) { }
    }

    fun sendGoalCompleted(context: Context, goalTitle: String, fameEarned: Int) {
        try {
            NotificationManagerCompat.from(context).notify(
                NOTIF_ID_GOAL,
                buildCustomNotification(
                    context, CHANNEL_GOALS, NOTIF_ID_GOAL,
                    "Goal Crushed! (+$fameEarned Fame)",
                    "You reached the milestone: $goalTitle",
                    0xFF20B2AA.toInt()
                )
            )
        } catch (_: SecurityException) { }
    }
}
