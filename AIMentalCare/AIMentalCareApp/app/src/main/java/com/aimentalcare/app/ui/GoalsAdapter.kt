package com.aimentalcare.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.aimentalcare.app.R
import com.aimentalcare.app.data.GoalEntity
import com.aimentalcare.app.databinding.ItemGoalBinding

class GoalsAdapter(
    private val onToggleComplete: (GoalEntity, Boolean) -> Unit,
    private val onDeleteClick: (GoalEntity) -> Unit
) : RecyclerView.Adapter<GoalsAdapter.GoalViewHolder>() {

    private val goals = mutableListOf<GoalEntity>()

    fun submitList(newGoals: List<GoalEntity>) {
        goals.clear()
        goals.addAll(newGoals)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(goals[position])
    }

    override fun getItemCount(): Int = goals.size

    inner class GoalViewHolder(private val binding: ItemGoalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: GoalEntity) {
            val ctx = binding.root.context
            binding.txtGoalTitle.text = goal.title
            binding.txtGoalCategory.text = goal.category

            val target = goal.targetDays.coerceAtLeast(1)
            val completed = if (goal.isCompleted) target else goal.completedDays.coerceIn(0, target)
            val percent = (completed.toFloat() / target * 100).toInt().coerceIn(0, 100)

            binding.progressGoal.progress = percent
            binding.txtGoalPercentage.text = "$percent%"

            binding.chkGoalCompleted.setOnCheckedChangeListener(null)
            binding.chkGoalCompleted.isChecked = goal.isCompleted

            if (goal.isCompleted) {
                binding.txtGoalProgress.text = "$completed of $target target days completed (Goal Achieved! 🎉)"
                binding.txtGoalTitle.paint.isStrikeThruText = true
                binding.txtGoalPercentage.setTextColor(ContextCompat.getColor(ctx, R.color.primary_green))
            } else {
                binding.txtGoalProgress.text = "$completed of $target target days completed ($percent%)"
                binding.txtGoalTitle.paint.isStrikeThruText = false
                val colorRes = if (percent >= 70) R.color.primary_green else if (percent >= 40) R.color.color_moderate else R.color.secondary_text
                binding.txtGoalPercentage.setTextColor(ContextCompat.getColor(ctx, colorRes))
            }

            binding.chkGoalCompleted.setOnCheckedChangeListener { _, isChecked ->
                onToggleComplete(goal, isChecked)
            }

            binding.btnDeleteGoal.setOnClickListener {
                onDeleteClick(goal)
            }
        }
    }
}
