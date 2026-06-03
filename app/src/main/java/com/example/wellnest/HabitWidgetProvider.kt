package com.example.wellnest

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class HabitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val prefs = context.getSharedPreferences("WellNestPrefs", Context.MODE_PRIVATE)
        val habits = prefs.getStringSet("habits", setOf()) ?: setOf()
        val completed = prefs.getStringSet("completedHabits", setOf()) ?: setOf()

        val percentage = if (habits.isNotEmpty()) {
            (completed.size * 100) / habits.size
        } else 0

        val progressText = "$percentage% habits completed"

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_habit)
            views.setTextViewText(R.id.widgetHabitText, progressText)
            views.setImageViewResource(R.id.widgetLogo, R.drawable.ic_wellnest_logo)

            // ✅ Add this: Open app → Habits page when tapped
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "habits") // 👈 custom extra
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(id, views)
        }
    }
}
