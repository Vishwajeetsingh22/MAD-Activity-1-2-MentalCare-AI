package com.aimentalcare.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aimentalcare.app.data.ChatMessageEntity
import com.aimentalcare.app.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(
    private var messages: List<ChatMessageEntity>,
    private val onEmergencyCall: () -> Unit
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun updateMessages(newMessages: List<ChatMessageEntity>) {
        this.messages = newMessages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class ChatViewHolder(private val binding: ItemChatMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessageEntity) {
            val formattedTime = timeFormat.format(Date(message.timestamp))

            if (message.sender == "USER") {
                binding.layoutUserMessage.visibility = View.VISIBLE
                binding.layoutAiMessage.visibility = View.GONE
                binding.txtUserMessage.text = message.messageText
                binding.txtUserTime.text = formattedTime
            } else {
                binding.layoutUserMessage.visibility = View.GONE
                binding.layoutAiMessage.visibility = View.VISIBLE
                binding.txtAiMessage.text = message.messageText
                binding.txtAiTime.text = formattedTime

                if (message.isCrisis) {
                    binding.btnCallEmergencyContactInChat.visibility = View.VISIBLE
                    binding.btnCallEmergencyContactInChat.setOnClickListener {
                        onEmergencyCall()
                    }
                } else {
                    binding.btnCallEmergencyContactInChat.visibility = View.GONE
                }
            }
        }
    }
}
