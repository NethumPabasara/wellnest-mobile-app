package com.example.wellnest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class HydrationReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            // ✅ Vibrate safely
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.VIBRATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    vibrator.vibrate(500)
                }
            }

            // ✅ Notification
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val notificationSound =
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val builder = NotificationCompat.Builder(context, "hydration_channel")
                    .setSmallIcon(R.mipmap.ic_launcher) // ✅ Safe fallback
                    .setContentTitle("Hydration Reminder")
                    .setContentText("Time to drink a glass of water 💧")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setSound(notificationSound)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(context)) {
                    notify(System.currentTimeMillis().toInt(), builder.build())
                }
            }

            Toast.makeText(context, "Hydration Reminder Triggered!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(context, "Receiver Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
