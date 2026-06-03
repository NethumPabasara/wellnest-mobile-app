package com.example.wellnest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnest.WidgetUtils  // ✅ added

class HabitAdapter(
    private val context: Context,
    private val habits: MutableList<String>,
    private val onOptions: (Int, String) -> Unit,
    private val onProgressChanged: (() -> Unit)? = null // ✅ callback to update progress
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkHabit: CheckBox = itemView.findViewById(R.id.checkHabit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.checkHabit.text = habit

        val prefs = context.getSharedPreferences("WellNestPrefs", Context.MODE_PRIVATE)
        val completed = prefs.getStringSet("completedHabits", setOf()) ?: setOf()

        // Avoid triggering listener when restoring state
        holder.checkHabit.setOnCheckedChangeListener(null)
        holder.checkHabit.isChecked = completed.contains(habit)

        // ✅ Save state + notify fragment + update widget
        holder.checkHabit.setOnCheckedChangeListener { _, isChecked ->
            val updated = prefs.getStringSet("completedHabits", setOf())?.toMutableSet() ?: mutableSetOf()
            if (isChecked) {
                updated.add(habit)
            } else {
                updated.remove(habit)
            }
            prefs.edit()
                .putStringSet("completedHabits", updated)
                .putInt("habitsCompleted", updated.size)
                .apply()

            onProgressChanged?.invoke()
            WidgetUtils.updateHabitWidgets(context) // ✅ added
        }

        // Edit/Delete options still work
        holder.itemView.setOnClickListener {
            onOptions(position, habit)
        }
    }

    override fun getItemCount(): Int = habits.size
}
