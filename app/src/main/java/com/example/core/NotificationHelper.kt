package com.example.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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

    fun sendPomodoroFinished(context: Context, subject: String, minutes: Int, fameEarned: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_POMODORO)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Focus Session Complete! (+$fameEarned Fame)")
            .setContentText("Great job studying $subject for $minutes min! Take a well-deserved break.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Great job! You crushed $minutes minutes of deep focus in $subject. +$fameEarned Fame added to your balance, and \$$subject stock is rising!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_POMODORO, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun sendBreakFinished(context: Context, nextRound: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_POMODORO)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Break is Over! (Round $nextRound/4)")
            .setContentText("Time to lock back in. Your study buddy is waiting at the desk!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_POMODORO, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun sendSavageAlert(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SAVAGE)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_SAVAGE, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun sendGoalCompleted(context: Context, goalTitle: String, fameEarned: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_GOALS)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle("Goal Crushed! (+$fameEarned Fame)")
            .setContentText("You reached the milestone: $goalTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_ID_GOAL, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}
