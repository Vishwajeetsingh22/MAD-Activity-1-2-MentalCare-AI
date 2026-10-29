package com.aimentalcare.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aimentalcare.app.databinding.ItemChatHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ConversationSummary(
    val conversationId: String,
    val title: String,
    val lastMessage: String,
    val timestamp: Long
)

class ChatHistoryAdapter(
    private val onItemClick: (ConversationSummary) -> Unit
) : RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder>() {

    private val items = mutableListOf<ConversationSummary>()
    private val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

    fun submitList(newItems: List<ConversationSummary>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHistoryViewHolder {
        val binding = ItemChatHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatHistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ChatHistoryViewHolder(private val binding: ItemChatHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ConversationSummary) {
            binding.txtHistoryTitle.text = item.title
            binding.txtHistoryPreview.text = item.lastMessage
            binding.txtHistoryTime.text = dateFormat.format(Date(item.timestamp))

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
