package com.example.wellnest

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var chart: LineChart
    private val PREFS_NAME = "WellNestPrefs"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar setup
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = "Profile"

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_notifications -> true
                R.id.action_settings -> true
                else -> false
            }
        }

        chart = view.findViewById(R.id.moodChart)
        setupMoodTrendChart()
    }

    override fun onResume() {
        super.onResume()
        if (this::chart.isInitialized) {
            setupMoodTrendChart()
        }
    }

    private fun setupMoodTrendChart() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val moods = prefs.getStringSet("moods", setOf()) ?: setOf()

        val moodMap = mutableMapOf<String, Int>()
        for (entry in moods) {
            val parts = entry.split("|")
            if (parts.size == 3) {
                val date = parts[0]
                val emoji = parts[2]

                val value = when (emoji) {
                    "😍" -> 5
                    "😊" -> 4
                    "😌" -> 3
                    "😐" -> 2
                    "😕", "😢", "😡" -> 1
                    else -> 0
                }
                moodMap[date] = maxOf(moodMap[date] ?: 0, value)
            }
        }

        val sdf = SimpleDateFormat("yyyy-M-d", Locale.getDefault())
        val entries = mutableListOf<Entry>()
        val labels = mutableListOf<String>()

        for (i in 6 downTo 0) {
            val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateStr = sdf.format(date.time)
            val label = SimpleDateFormat("EEE", Locale.getDefault()).format(date.time)
            val moodValue = moodMap[dateStr] ?: 0
            entries.add(Entry((7 - i).toFloat(), moodValue.toFloat()))
            labels.add(label)
        }

        val dataSet = LineDataSet(entries, "Mood Score").apply {
            color = Color.parseColor("#4CAF50")
            setCircleColor(Color.parseColor("#03A9F4"))
            lineWidth = 2f
            circleRadius = 5f
            valueTextSize = 10f
            valueTextColor = Color.DKGRAY
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        chart.data = LineData(dataSet)
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = false

        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 5f
        leftAxis.textColor = Color.BLACK // Ensure emoji visibility

        // 🌈 Emoji-based Y-axis labels
        leftAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when (value.toInt()) {
                    5 -> "😍"
                    4 -> "😊"
                    3 -> "😌"
                    2 -> "😐"
                    1 -> "😢"
                    else -> ""
                }
            }
        }

        // 🎨 Mood color zones (with gradient background)
        chart.setBackgroundResource(R.drawable.mood_chart_bg)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.DKGRAY
        xAxis.granularity = 1f
        xAxis.labelCount = 7
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        chart.animateY(1000)
        chart.invalidate()
    }
}
