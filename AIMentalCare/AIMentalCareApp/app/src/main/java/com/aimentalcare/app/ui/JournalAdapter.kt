package com.aimentalcare.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.aimentalcare.app.data.JournalEntity
import com.aimentalcare.app.databinding.ItemJournalBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JournalAdapter(
    private val onAnalyzeClick: (JournalEntity) -> Unit,
    private val onEditClick: (JournalEntity) -> Unit,
    private val onDeleteClick: (JournalEntity) -> Unit
) : RecyclerView.Adapter<JournalAdapter.JournalViewHolder>() {

    private val entries = mutableListOf<JournalEntity>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    fun submitList(newEntries: List<JournalEntity>) {
        entries.clear()
        entries.addAll(newEntries)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val binding = ItemJournalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JournalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int = entries.size

    inner class JournalViewHolder(private val binding: ItemJournalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: JournalEntity) {
            binding.txtJournalTitle.text = entry.title
            binding.txtJournalContent.text = entry.content
            binding.txtJournalMood.text = entry.moodTag
            binding.txtJournalDate.text = dateFormat.format(Date(entry.timestamp))

            if (entry.aiAnalyzed && entry.aiStressLevel != null) {
                binding.layoutAiBadge.visibility = View.VISIBLE
                binding.btnAnalyzeWithAi.visibility = View.GONE
                binding.txtAiStressLevel.text = "🧠 AI Wellness Analysis: ${entry.aiStressLevel} STRESS"

                val colorHex = when (entry.aiStressLevel) {
                    "LOW" -> "#10B981"
                    "MODERATE" -> "#F59E0B"
                    else -> "#EF4444"
                }
                binding.txtAiStressLevel.setTextColor(colorHex.toColorInt())
                binding.txtAiRecommendation.text = entry.aiRecommendations ?: "Recommendation: Keep up your mindful reflection."
            } else {
                binding.layoutAiBadge.visibility = View.GONE
                binding.btnAnalyzeWithAi.visibility = View.VISIBLE
            }

            binding.btnAnalyzeWithAi.setOnClickListener {
                onAnalyzeClick(entry)
            }

            binding.btnEditJournal.setOnClickListener {
                onEditClick(entry)
            }

            binding.btnDeleteJournal.setOnClickListener {
                onDeleteClick(entry)
            }
        }
    }
}
