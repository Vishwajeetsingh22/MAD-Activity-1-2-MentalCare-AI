package com.aimentalcare.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aimentalcare.app.api.ApiClient
import com.aimentalcare.app.api.ChatRequest
import com.aimentalcare.app.data.ChatMessageEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.FragmentChatbotBinding
import com.aimentalcare.app.utils.LocalChatEngine
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: MentalCareDatabase
    private lateinit var adapter: ChatAdapter
    private var currentUserId: String = "user_default"
    private var currentConversationId: String = "active_session"
    private var latestStressLevel: String? = null

    companion object {
        private const val ARG_CONVERSATION_ID = "arg_conv_id"

        fun newInstance(conversationId: String?): ChatbotFragment {
            val fragment = ChatbotFragment()
            if (conversationId != null) {
                val args = Bundle().apply {
                    putString(ARG_CONVERSATION_ID, conversationId)
                }
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ctx = requireContext()
        db = MentalCareDatabase.getDatabase(ctx)

        val prefs = ctx.getSharedPreferences("MentalCarePrefs", Context.MODE_PRIVATE)
        currentUserId = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

        val passedConvId = arguments?.getString(ARG_CONVERSATION_ID)
        currentConversationId = passedConvId
            ?: prefs.getString("active_chat_conv_id", "conv_${System.currentTimeMillis()}")
            ?: "conv_${System.currentTimeMillis()}"

        prefs.edit().putString("active_chat_conv_id", currentConversationId).apply()

        setupRecyclerView()
        observeStressContext()
        observeChatMessages()

        binding.btnChatBack.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                (activity as? MainActivity)?.navigateToTab(com.aimentalcare.app.R.id.nav_dashboard)
            }
        }

        binding.btnSendMessage.setOnClickListener {
            sendMessage()
        }

        binding.btnNewChat.setOnClickListener {
            startNewChat()
        }

        binding.btnChatHistory.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(ChatHistoryFragment())
        }

        binding.btnClearChat.setOnClickListener {
            showClearChatConfirmation()
        }

        binding.chipRelax.setOnClickListener {
            binding.etChatMessage.setText("Can you guide me through a calming breathing exercise to help me relax?")
            sendMessage()
        }

        binding.chipStressed.setOnClickListener {
            binding.etChatMessage.setText("I'm feeling quite stressed and overwhelmed today. What can I do right now to feel better?")
            sendMessage()
        }

        binding.chipPlanDay.setOnClickListener {
            binding.etChatMessage.setText("Help me structure my day and break down my tasks into manageable, low-stress steps.")
            sendMessage()
        }

        binding.chipSleep.setOnClickListener {
            binding.etChatMessage.setText("What are some relaxing techniques to help me unwind and sleep better tonight?")
            sendMessage()
        }

        binding.chipRecentCheckIns.setOnClickListener {
            binding.etChatMessage.setText("Tell me about my recent check-ins and how my stress has been tracking.")
            sendMessage()
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(
            emptyList(),
            onEmergencyCall = { triggerEmergencyCallAction() }
        )
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.recyclerChat.layoutManager = layoutManager
        binding.recyclerChat.adapter = adapter
    }

    private fun observeStressContext() {
        db.checkInDao().getLatestCheckIn().observe(viewLifecycleOwner) { checkIn ->
            val b = _binding ?: return@observe
            if (checkIn != null) {
                latestStressLevel = checkIn.stressLevel
                b.txtChatStressContext.text = "🌿 Context: Latest Check-in ${checkIn.stressLevel} (${checkIn.stressScore}%)"
            } else {
                b.txtChatStressContext.text = "🌿 Context: Normal wellness baseline"
            }
        }
    }

    private fun observeChatMessages() {
        db.checkInDao().getChatMessages(currentUserId, currentConversationId).observe(viewLifecycleOwner) { messages ->
            val b = _binding ?: return@observe
            adapter.updateMessages(messages)
            if (messages.isNotEmpty()) {
                b.recyclerChat.scrollToPosition(messages.size - 1)
            } else {
                insertWelcomeGreeting()
            }
        }
    }

    private fun insertWelcomeGreeting() {
        lifecycleScope.launch {
            val welcomeText = "Hello! I am your AI MentalCare Assistant. Talk to me about how you're feeling today—whether it's academic deadlines, exams, sleep, or simply wanting a calm pause. How can I support you right now?"
            db.checkInDao().insertChatMessage(
                ChatMessageEntity(
                    userId = currentUserId,
                    conversationId = currentConversationId,
                    sender = "AI",
                    messageText = welcomeText,
                    isCrisis = false
                )
            )
        }
    }

    private fun sendMessage() {
        val messageText = binding.etChatMessage.text.toString().trim()
        if (messageText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a message first.", Toast.LENGTH_SHORT).show()
            return
        }

        // Prevent duplicate clicks by disabling send button during processing
        binding.btnSendMessage.isEnabled = false
        binding.etChatMessage.text.clear()
        binding.layoutAiThinking.visibility = View.VISIBLE

        lifecycleScope.launch {
            val userMsg = ChatMessageEntity(
                userId = currentUserId,
                conversationId = currentConversationId,
                sender = "USER",
                messageText = messageText,
                isCrisis = false
            )
            db.checkInDao().insertChatMessage(userMsg)
            com.aimentalcare.app.data.FirebaseSyncHelper.syncChatToCloud(requireContext(), userMsg)

            try {
                val response = ApiClient.service.sendChatMessage(
                    ChatRequest(
                        userId = currentUserId,
                        message = messageText,
                        conversationId = currentConversationId,
                        currentStressLevel = latestStressLevel
                    )
                )

                if (response.isSuccessful && response.body() != null) {
                    val chatBody = response.body()!!
                    val aiMsg = ChatMessageEntity(
                        userId = currentUserId,
                        conversationId = currentConversationId,
                        sender = "AI",
                        messageText = chatBody.reply,
                        isCrisis = chatBody.isCrisis
                    )
                    db.checkInDao().insertChatMessage(aiMsg)
                    com.aimentalcare.app.data.FirebaseSyncHelper.syncChatToCloud(requireContext(), aiMsg)
                } else {
                    val code = response.code()
                    handleNetworkOrBackendFailure(messageText, "Server returned error HTTP $code. Check backend server logs.")
                }
            } catch (e: Exception) {
                handleNetworkOrBackendFailure(messageText, "Unable to reach server (${e.localizedMessage ?: "Connection error"}).")
            } finally {
                _binding?.layoutAiThinking?.visibility = View.GONE
                _binding?.btnSendMessage?.isEnabled = true
            }
        }
    }

    private suspend fun handleNetworkOrBackendFailure(userText: String, errorDetail: String) {
        // Transparent error notice so users know the network status
        val noticeText = "⚠️ Backend Offline Notice: $errorDetail\n\nPlease start the backend server by running `py app.py` on port 5000."
        val noticeMsg = ChatMessageEntity(
            userId = currentUserId,
            conversationId = currentConversationId,
            sender = "AI",
            messageText = noticeText,
            isCrisis = false
        )
        db.checkInDao().insertChatMessage(noticeMsg)

        // Provide context-aware on-device assistance so the user is never stranded
        val localResult = LocalChatEngine.respond(userText, currentConversationId, latestStressLevel)
        val localMsg = ChatMessageEntity(
            userId = currentUserId,
            conversationId = currentConversationId,
            sender = "AI",
            messageText = "[On-Device Wellness Support]\n" + localResult.reply,
            isCrisis = localResult.isCrisis
        )
        db.checkInDao().insertChatMessage(localMsg)

        val root = _binding?.root ?: return
        Snackbar.make(root, "Backend unreachable. On-device wellness guidance provided.", Snackbar.LENGTH_LONG)
            .setAction("Retry") {
                binding.etChatMessage.setText(userText)
                sendMessage()
            }
            .show()
    }

    private fun startNewChat() {
        currentConversationId = "conv_${System.currentTimeMillis()}"
        val prefs = requireContext().getSharedPreferences("MentalCarePrefs", Context.MODE_PRIVATE)
        prefs.edit().putString("active_chat_conv_id", currentConversationId).apply()
        LocalChatEngine.clearSession(currentConversationId)
        observeChatMessages()
        Toast.makeText(requireContext(), "Started new conversation.", Toast.LENGTH_SHORT).show()
    }

    private fun showClearChatConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear Chat History")
            .setMessage("Are you sure you want to delete all messages in this conversation?")
            .setPositiveButton("Clear") { _, _ ->
                lifecycleScope.launch {
                    db.checkInDao().clearChatMessages(currentUserId, currentConversationId)
                    LocalChatEngine.clearSession(currentConversationId)
                    insertWelcomeGreeting()
                    Toast.makeText(requireContext(), "Chat history cleared.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun triggerEmergencyCallAction() {
        val prefs = requireContext().getSharedPreferences("MentalCarePrefs", Context.MODE_PRIVATE)
        val sosNum = prefs.getString("sos_emergency_number", "+91 98888 00000") ?: "+91 98888 00000"

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Emergency Call")
            .setMessage("Are you sure you want to call your emergency contact ($sosNum)?")
            .setPositiveButton("Call") { _, _ ->
                try {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sosNum"))
                    startActivity(dialIntent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Unable to start the call.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
