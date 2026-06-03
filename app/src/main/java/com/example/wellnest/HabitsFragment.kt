package com.example.wellnest

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitAdapter
    private lateinit var habits: MutableList<String>
    private val PREFS_NAME = "WellNestPrefs"
    private val KEY_HABITS = "habits"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_habits, container, false)
    }

    private fun getTodayDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = getTodayDate()
        val lastDate = prefs.getString("lastHabitDate", "")

        // ✅ Reset progress if it's a new day
        if (lastDate != today) {
            prefs.edit()
                .putString("lastHabitDate", today)
                .putStringSet("completedHabits", emptySet())
                .putInt("habitsCompleted", 0)
                .apply()

            WidgetUtils.updateHabitWidgets(requireContext()) // ✅ added
        }

        // Toolbar
        val toolbar = view.findViewById<MaterialToolbar>(R.id.topAppBar)
        val toolbarTitle = view.findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = "Habits"

        // Load habits
        habits = prefs.getStringSet(KEY_HABITS, setOf())?.toMutableList() ?: mutableListOf()

        // RecyclerView
        recyclerView = view.findViewById(R.id.recyclerHabits)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = HabitAdapter(
            requireContext(),
            habits,
            onOptions = { index, habit ->
                showOptionsDialog(index, habit)
                updateProgressText() // ✅ Refresh when edit/delete
                WidgetUtils.updateHabitWidgets(requireContext()) // ✅ added
            },
            onProgressChanged = {
                updateProgressText() // ✅ Refresh when checkbox toggled
                WidgetUtils.updateHabitWidgets(requireContext()) // ✅ added
            }
        )
        recyclerView.adapter = adapter

        // 🔽 Add these lines right below recyclerView.adapter = adapter
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.setHasFixedSize(false)


        // Add habit
        val btnAdd = view.findViewById<FloatingActionButton>(R.id.btnAddHabit)
        btnAdd.setOnClickListener {
            showAddHabitDialog()
        }

        // ✅ Initial update
        updateProgressText()
    }

    private fun showAddHabitDialog() {
        val editText = EditText(requireContext())
        AlertDialog.Builder(requireContext())
            .setTitle("Add New Habit")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val habit = editText.text.toString()
                if (habit.isNotEmpty()) {
                    habits.add(habit)
                    adapter.notifyDataSetChanged()
                    saveHabits()
                    updateProgressText() // ✅ Update after adding
                    WidgetUtils.updateHabitWidgets(requireContext()) // ✅ added
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveHabits() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_HABITS, habits.toSet()).apply()
    }

    private fun showOptionsDialog(index: Int, habit: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Choose Action")
            .setMessage("What do you want to do with \"$habit\"?")
            .setPositiveButton("Edit") { _, _ ->
                showEditHabitDialog(index, habit)
            }
            .setNegativeButton("Delete") { _, _ ->
                confirmDeleteHabit(habit)
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showEditHabitDialog(index: Int, oldHabit: String) {
        val editText = EditText(requireContext())
        editText.setText(oldHabit)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(editText)
            .setPositiveButton("Update") { _, _ ->
                val newHabit = editText.text.toString()
                if (newHabit.isNotEmpty()) {
                    habits[index] = newHabit

                    // Carry over completion state
                    val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val set = prefs.getStringSet("completedHabits", setOf())?.toMutableSet() ?: mutableSetOf()
                    if (set.remove(oldHabit)) {
                        set.add(newHabit)
                        prefs.edit()
                            .putStringSet("completedHabits", set)
                            .putInt("habitsCompleted", set.size)
                            .apply()
                    }

                    adapter.notifyDataSetChanged()
                    saveHabits()
                    updateProgressText() // ✅ Update after edit
                    WidgetUtils.updateHabitWidgets(requireContext()) // ✅ added
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteHabit(habit: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Do you really want to delete \"$habit\"?")
            .setPositiveButton("Delete") { _, _ ->
                habits.remove(habit)

                val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val set = prefs.getStringSet("completedHabits", setOf())?.toMutableSet() ?: mutableSetOf()
                if (set.remove(habit)) {
                    prefs.edit()
                        .putStringSet("completedHabits", set)
                        .putInt("habitsCompleted", set.size)
                        .apply()
                }

                adapter.notifyDataSetChanged()
                saveHabits()
                updateProgressText() // ✅ Update after delete
                WidgetUtils.updateHabitWidgets(requireContext()) // ✅ added
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ✅ Helper to refresh 0/0 → X/Y habits completed
    private fun updateProgressText() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val allHabits = prefs.getStringSet(KEY_HABITS, setOf()) ?: setOf()
        val completed = prefs.getStringSet("completedHabits", setOf()) ?: setOf()

        val tvProgress = view?.findViewById<TextView>(R.id.tvHabitsProgress)
        if (allHabits.isNotEmpty()) {
            tvProgress?.text = "${completed.size}/${allHabits.size} habits completed"
        } else {
            tvProgress?.text = "0/0 habits completed"
        }
    }
}
