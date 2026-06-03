package com.example.wellnest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Now moods are Triple<date, time, emoji>
class MoodAdapter(private var moods: List<Triple<String, String, String>>) :
    RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvMoodDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvMoodTime)
        val tvEmoji: TextView = itemView.findViewById(R.id.tvMoodEmoji)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val (date, time, emoji) = moods[position]
        holder.tvDate.text = date          // e.g. 2025-10-03
        holder.tvTime.text = time          // e.g. 14:32
        holder.tvEmoji.text = emoji        // e.g. 😊
    }

    override fun getItemCount(): Int = moods.size

    fun updateData(newMoods: List<Triple<String, String, String>>) {
        moods = newMoods
        notifyDataSetChanged()
    }
}
