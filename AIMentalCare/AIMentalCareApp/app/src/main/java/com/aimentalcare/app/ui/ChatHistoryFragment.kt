package com.aimentalcare.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aimentalcare.app.R
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.FragmentChatHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatHistoryFragment : Fragment() {

    private var _binding: FragmentChatHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatHistoryAdapter
    private var userEmail: String = "user@mentalcare.ai"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

        setupRecyclerView()
        setupListeners()
        loadConversations()
    }

    private fun setupRecyclerView() {
        adapter = ChatHistoryAdapter { summary ->
            // Open this conversation in ChatbotFragment
            val fragment = ChatbotFragment.newInstance(summary.conversationId)
            (activity as? MainActivity)?.replaceFragment(fragment)
        }
        binding.recyclerHistory.layoutManager = LinearLayoutManager(context)
        binding.recyclerHistory.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnHistoryBack.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(ChatbotFragment())
        }

        binding.btnNewChatFromHistory.setOnClickListener {
            val fragment = ChatbotFragment.newInstance(null) // null generates new conversation
            (activity as? MainActivity)?.replaceFragment(fragment)
        }
    }

    private fun loadConversations() {
        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)

        lifecycleScope.launch(Dispatchers.IO) {
            val convIds = db.checkInDao().getDistinctConversationsSync(userEmail)
            val summaries = mutableListOf<ConversationSummary>()

            for (convId in convIds) {
                val latestMsg = db.checkInDao().getLatestMessageForConversation(userEmail, convId)
                if (latestMsg != null) {
                    val preview = latestMsg.messageText
                    val title = if (preview.length > 30) preview.take(30) + "..." else preview
                    summaries.add(
                        ConversationSummary(
                            conversationId = convId,
                            title = title,
                            lastMessage = preview,
                            timestamp = latestMsg.timestamp
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                val b = _binding ?: return@withContext
                adapter.submitList(summaries)
                b.layoutHistoryEmpty.visibility = if (summaries.isEmpty()) View.VISIBLE else View.GONE
                b.recyclerHistory.visibility = if (summaries.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
