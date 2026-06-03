package com.example.wellnest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.widget.Toast
import android.app.TimePickerDialog
import android.os.Build
import android.net.Uri

class HydrationFragment : Fragment() {

    private lateinit var tvProgress: TextView
    private lateinit var tvMotivation: TextView
    private lateinit var btnAddWater: FloatingActionButton
    private lateinit var btnResetDay: MaterialButton

    private lateinit var btnSetAlarm1: MaterialButton
    private lateinit var btnSetAlarm2: MaterialButton
    private lateinit var btnSetAlarm3: MaterialButton
    private lateinit var btnSetAlarm4: MaterialButton
    private lateinit var btnSetAlarm5: MaterialButton
    private lateinit var btnSetAlarm6: MaterialButton
    private lateinit var btnSetAlarm7: MaterialButton
    private lateinit var btnSetAlarm8: MaterialButton

    private lateinit var tvAlarm1: TextView
    private lateinit var tvAlarm2: TextView
    private lateinit var tvAlarm3: TextView
    private lateinit var tvAlarm4: TextView
    private lateinit var tvAlarm5: TextView
    private lateinit var tvAlarm6: TextView
    private lateinit var tvAlarm7: TextView
    private lateinit var tvAlarm8: TextView

    private lateinit var glassViews: List<TextView>

    private val PREFS_NAME = "WellNestPrefs"
    private val KEY_WATER = "waterCount"
    private val KEY_DATE = "lastDate"

    private var waterCount = 0
    private val dailyGoal = 8

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hydration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = "Hydration"

        tvProgress = view.findViewById(R.id.tvHydrationProgress)
        tvMotivation = view.findViewById(R.id.tvMotivation)
        btnAddWater = view.findViewById(R.id.btnAddWater)
        btnResetDay = view.findViewById(R.id.btnResetDay)

        btnSetAlarm1 = view.findViewById(R.id.btnSetAlarm1)
        btnSetAlarm2 = view.findViewById(R.id.btnSetAlarm2)
        btnSetAlarm3 = view.findViewById(R.id.btnSetAlarm3)
        btnSetAlarm4 = view.findViewById(R.id.btnSetAlarm4)
        btnSetAlarm5 = view.findViewById(R.id.btnSetAlarm5)
        btnSetAlarm6 = view.findViewById(R.id.btnSetAlarm6)
        btnSetAlarm7 = view.findViewById(R.id.btnSetAlarm7)
        btnSetAlarm8 = view.findViewById(R.id.btnSetAlarm8)

        tvAlarm1 = view.findViewById(R.id.tvAlarm1)
        tvAlarm2 = view.findViewById(R.id.tvAlarm2)
        tvAlarm3 = view.findViewById(R.id.tvAlarm3)
        tvAlarm4 = view.findViewById(R.id.tvAlarm4)
        tvAlarm5 = view.findViewById(R.id.tvAlarm5)
        tvAlarm6 = view.findViewById(R.id.tvAlarm6)
        tvAlarm7 = view.findViewById(R.id.tvAlarm7)
        tvAlarm8 = view.findViewById(R.id.tvAlarm8)

        glassViews = listOf(
            view.findViewById(R.id.glass1),
            view.findViewById(R.id.glass2),
            view.findViewById(R.id.glass3),
            view.findViewById(R.id.glass4),
            view.findViewById(R.id.glass5),
            view.findViewById(R.id.glass6),
            view.findViewById(R.id.glass7),
            view.findViewById(R.id.glass8)
        )

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedDate = prefs.getString(KEY_DATE, "")
        val today = getTodayDate()

        // Reset daily progress if new day
        if (savedDate != today) {
            waterCount = 0
            prefs.edit().putInt(KEY_WATER, waterCount).putString(KEY_DATE, today).apply()
        } else {
            waterCount = prefs.getInt(KEY_WATER, 0)
        }

        updateUI()

        // ✅ Restore alarm labels on fragment load
        restoreAlarmLabels(prefs)

        btnAddWater.setOnClickListener {
            if (waterCount < dailyGoal) {
                waterCount++
                prefs.edit().putInt(KEY_WATER, waterCount).putString(KEY_DATE, today).apply()
                updateUI()
            }
        }

        btnResetDay.setOnClickListener {
            waterCount = 0
            prefs.edit().putInt(KEY_WATER, waterCount).putString(KEY_DATE, today).apply()
            updateUI()
        }

        // --- Alarm Buttons ---
        setupAlarmButton(btnSetAlarm1, tvAlarm1, 1)
        setupAlarmButton(btnSetAlarm2, tvAlarm2, 2)
        setupAlarmButton(btnSetAlarm3, tvAlarm3, 3)
        setupAlarmButton(btnSetAlarm4, tvAlarm4, 4)
        setupAlarmButton(btnSetAlarm5, tvAlarm5, 5)
        setupAlarmButton(btnSetAlarm6, tvAlarm6, 6)
        setupAlarmButton(btnSetAlarm7, tvAlarm7, 7)
        setupAlarmButton(btnSetAlarm8, tvAlarm8, 8)
    }

    private fun updateUI() {
        tvProgress.text = "$waterCount / $dailyGoal glasses"
        for (i in glassViews.indices) {
            glassViews[i].text = if (i < waterCount) "🥛" else "⬜"
        }

        tvMotivation.text = when {
            waterCount == 0 -> "Let’s get started 💧"
            waterCount in 1..3 -> "Good start, keep sipping 🚰"
            waterCount in 4..6 -> "Halfway there! 💪"
            waterCount == 7 -> "Almost done, one more to go! 🔥"
            waterCount >= dailyGoal -> "Great job! You reached your goal 🎉"
            else -> ""
        }
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    // ✅ Generic alarm button setup
    private fun setupAlarmButton(button: MaterialButton, textView: TextView, requestCode: Int) {
        button.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, 0)

                val label = "Alarm $requestCode: ${String.format("%02d:%02d", selectedHour, selectedMinute)}"
                setAlarm(calendar.timeInMillis, requestCode, label)
                textView.text = label
            }, hour, minute, true).show()
        }
    }

    private fun setAlarm(triggerTime: Long, requestCode: Int, label: String) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(requireContext(), "Please allow exact alarms in settings ⚙️", Toast.LENGTH_LONG).show()
                val intentSettings = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intentSettings.data = Uri.parse("package:" + requireContext().packageName)
                startActivity(intentSettings)
                return
            }
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

        // Save alarm time + label
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("alarm$requestCode", triggerTime)
            .putString("alarm${requestCode}_text", label)
            .apply()

        Toast.makeText(requireContext(), "Alarm set ⏰", Toast.LENGTH_SHORT).show()
    }

    // ✅ Restore all alarm labels
    private fun restoreAlarmLabels(prefs: android.content.SharedPreferences) {
        for (i in 1..8) {
            val savedText = prefs.getString("alarm${i}_text", null)
            if (savedText != null) {
                when (i) {
                    1 -> tvAlarm1.text = savedText
                    2 -> tvAlarm2.text = savedText
                    3 -> tvAlarm3.text = savedText
                    4 -> tvAlarm4.text = savedText
                    5 -> tvAlarm5.text = savedText
                    6 -> tvAlarm6.text = savedText
                    7 -> tvAlarm7.text = savedText
                    8 -> tvAlarm8.text = savedText
                }
            }
        }
    }
}
