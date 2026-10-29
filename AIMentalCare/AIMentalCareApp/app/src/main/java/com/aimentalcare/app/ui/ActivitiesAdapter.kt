package com.aimentalcare.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aimentalcare.app.databinding.ItemWellnessActivityBinding

data class ActivityItem(
    val id: String,
    val icon: String,
    val title: String,
    val category: String,
    val durationMinutes: Int,
    val description: String,
    val guidance: String
)

class ActivitiesAdapter(
    private val onStartClick: (ActivityItem) -> Unit
) : RecyclerView.Adapter<ActivitiesAdapter.ActivityViewHolder>() {

    private val items = mutableListOf<ActivityItem>()

    fun submitList(newItems: List<ActivityItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val binding = ItemWellnessActivityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActivityViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ActivityViewHolder(private val binding: ItemWellnessActivityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ActivityItem) {
            binding.txtActivityIcon.text = item.icon
            binding.txtActivityTitle.text = item.title
            binding.txtActivityCategory.text = "${item.category} • ${item.durationMinutes} min"
            binding.txtActivityDesc.text = item.description

            binding.btnStartActivity.setOnClickListener {
                onStartClick(item)
            }
        }
    }
}
