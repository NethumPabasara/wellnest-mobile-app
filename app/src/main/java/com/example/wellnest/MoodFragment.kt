package com.example.wellnest

import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var recyclerToday: RecyclerView
    private lateinit var todayAdapter: MoodAdapter
    private lateinit var recyclerSelected: RecyclerView
    private lateinit var selectedAdapter: MoodAdapter

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var tvTodayLog: TextView
    private lateinit var tvSelectedLog: TextView

    private val PREFS_NAME = "WellNestPrefs"
    private lateinit var prefs: android.content.SharedPreferences

    private var todayDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mood, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = "Mood Journal"

        prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        tvTodayLog = view.findViewById(R.id.tvTodayLog)
        tvSelectedLog = view.findViewById(R.id.tvSelectedDateLog)
        calendarView = view.findViewById(R.id.calendarView)

        val cal = Calendar.getInstance()
        todayDate = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"

        recyclerToday = view.findViewById(R.id.recyclerTodayMoods)
        recyclerToday.layoutManager = LinearLayoutManager(requireContext())
        todayAdapter = MoodAdapter(listOf())
        recyclerToday.adapter = todayAdapter

        recyclerSelected = view.findViewById(R.id.recyclerMoods)
        recyclerSelected.layoutManager = LinearLayoutManager(requireContext())
        selectedAdapter = MoodAdapter(listOf())
        recyclerSelected.adapter = selectedAdapter

        calendarView.setOnDateChangedListener { _, date, _ ->
            val clickedDate = "${date.year}-${date.month + 1}-${date.day}"
            val moodsForDate = loadMoods(prefs).filter { it.first == clickedDate }
            selectedAdapter.updateData(moodsForDate)
            tvSelectedLog.text = "Mood Log ($clickedDate)"
        }

        // ✅ Updated: 7 mood options now
        listOf(
            R.id.emoji_love to "😍",
            R.id.emoji_happy to "😊",
            R.id.emoji_relaxed to "😌",
            R.id.emoji_neutral to "😐",
            R.id.emoji_anxious to "😕",
            R.id.emoji_sad to "😢",
            R.id.emoji_angry to "😡"
        ).forEach { (id, emoji) ->
            view.findViewById<TextView>(id).setOnClickListener {
                saveMood(todayDate, emoji)
                refreshUI()
            }
        }

        refreshUI()
    }

    private fun saveMood(date: String, emoji: String) {
        val timestamp = System.currentTimeMillis()
        val set = prefs.getStringSet("moods", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add("$date|$timestamp|$emoji")
        prefs.edit().putStringSet("moods", set).apply()
    }

    private fun loadMoods(prefs: android.content.SharedPreferences): List<Triple<String, String, String>> {
        return prefs.getStringSet("moods", setOf())?.mapNotNull {
            val parts = it.split("|")
            if (parts.size == 3) {
                val date = parts[0]
                val time = try {
                    DateFormat.format("HH:mm", parts[1].toLong()).toString()
                } catch (e: Exception) {
                    "--:--"
                }
                val emoji = parts[2]
                Triple(date, time, emoji)
            } else null
        }?.sortedWith(compareByDescending<Triple<String, String, String>> { it.first }
            .thenByDescending { it.second })
            ?: listOf()
    }

    private fun refreshUI() {
        val moods = loadMoods(prefs)
        val todayMoods = moods.filter { it.first == todayDate }

        tvTodayLog.text = if (todayMoods.isNotEmpty())
            "Today's Mood Log" else "No moods logged today yet."

        todayAdapter.updateData(todayMoods)
        decorateCalendar(calendarView, moods)
        selectedAdapter.updateData(listOf())
        tvSelectedLog.text = "Mood Log (select a date)"
    }

    private fun decorateCalendar(calendarView: MaterialCalendarView, moods: List<Triple<String, String, String>>) {
        calendarView.removeDecorators()
        val grouped = moods.groupBy { it.first }
        grouped.forEach { (date, _) ->
            val parts = date.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val day = parts[2].toInt()
                val calendarDay = CalendarDay.from(year, month, day)
                calendarView.addDecorator(object : DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay) = day == calendarDay
                    override fun decorate(view: DayViewFacade) {
                        view.addSpan(DotSpan(10f, android.graphics.Color.BLUE))
                    }
                })
            }
        }
    }
}
