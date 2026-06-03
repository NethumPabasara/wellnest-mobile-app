package com.example.wellnest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import android.widget.TextView
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.content.Context
import android.text.format.DateFormat

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- ✅ Hydration Progress + Countdown ---
        val tvHydration = view.findViewById<TextView>(R.id.tvHydration)
        val tvCountdown = view.findViewById<TextView>(R.id.tvNextAlarmCountdown)

        val prefs = requireContext().getSharedPreferences("WellNestPrefs", Context.MODE_PRIVATE)
        val waterCount = prefs.getInt("waterCount", 0)
        val dailyGoal = 8
        tvHydration.text = "$waterCount / $dailyGoal glasses today"

        val now = System.currentTimeMillis()
        val alarms = (1..8).map { prefs.getLong("alarm$it", 0L) }
        val nextAlarm = alarms.filter { it > now }.minOrNull()

        if (nextAlarm != null) {
            val diff = nextAlarm - now
            val hours = (diff / 1000) / 3600
            val minutes = ((diff / 1000) % 3600) / 60
            val seconds = (diff / 1000) % 60
            tvCountdown.text = "Next reminder in: ${hours}h ${minutes}m ${seconds}s"
        } else {
            tvCountdown.text = "No reminders set ⏰"
        }

        // --- ✅ Habits Progress ---
        val habits = prefs.getStringSet("habits", setOf()) ?: setOf()
        val completedSet = prefs.getStringSet("completedHabits", setOf()) ?: setOf()
        val tvHabitProgress = view.findViewById<TextView>(R.id.tvHabitProgress)
        if (habits.isNotEmpty()) {
            tvHabitProgress.text = "${completedSet.size}/${habits.size} habits completed"
        } else {
            tvHabitProgress.text = "No habits yet"
        }

        // --- 🌿 Today’s Mood (from saved mood data) ---
        val tvTodayMood = view.findViewById<TextView>(R.id.tvTodayMood)
        val moods = prefs.getStringSet("moods", setOf()) ?: setOf()
        if (moods.isNotEmpty()) {
            val latest = moods.mapNotNull {
                val parts = it.split("|")
                if (parts.size == 3) parts else null
            }.maxByOrNull { it[1].toLongOrNull() ?: 0L }

            latest?.let {
                val date = it[0]
                val time = try {
                    DateFormat.format("HH:mm", it[1].toLong()).toString()
                } catch (e: Exception) {
                    "--:--"
                }
                val emoji = it[2]
                tvTodayMood.text = "$emoji  Logged on $date at $time"
            }
        } else {
            tvTodayMood.text = "No mood logged yet."
        }

        // --- ✅ Toolbar setup ---
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = "Home"

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notifications -> true
                R.id.action_settings -> true
                else -> false
            }
        }

        // --- ✅ Motivation rotation ---
        val quotes = arrayOf(
            "Stay strong, stay positive 🌱",
            "Drink water, refresh your mind 💧",
            "Small steps, lead to big results 🚀",
            "Your health is your wealth ❤️"
        )

        val images = arrayOf(
            R.drawable.motivation_leaf,
            R.drawable.motivation_water,
            R.drawable.motivation_steps,
            R.drawable.motivation_heart
        )

        val motivationText = view.findViewById<TextView>(R.id.motivationText)
        val motivationImage = view.findViewById<ImageView>(R.id.motivationImage)

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val index = (quotes.indices).random()
                motivationText.text = quotes[index]
                motivationImage.setImageResource(images[index])
                handler.postDelayed(this, 5000)
            }
        }
        handler.post(runnable)
    }
}
