package com.aimentalcare.app.ui

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aimentalcare.app.R
import com.aimentalcare.app.api.ApiClient
import com.aimentalcare.app.api.TextAnalyzeRequest
import com.aimentalcare.app.data.DailyPlanEntity
import com.aimentalcare.app.data.JournalEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.DialogAddEditJournalBinding
import com.aimentalcare.app.databinding.FragmentJournalBinding
import com.aimentalcare.app.utils.LocalNLPEngine
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Phase 6 — Premium Journal Fragment
 * Provides a calm, natural writing experience:
 * - "How are you feeling today?" canvas with generous multiline spacing
 * - Mood selector: 😊 Great, 🙂 Good, 😐 Okay, 😟 Low, 😣 Difficult
 * - Direct "Save Entry ✨" action without bureaucratic form fields
 * - Past reflections list with search, AI stress analysis, edit, and delete
 */
class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: JournalAdapter
    private var userEmail: String = "user@mentalcare.ai"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

        setupRecyclerView()
        setupListeners()
        loadJournals()
    }

    private fun setupRecyclerView() {
        adapter = JournalAdapter(
            onAnalyzeClick = { entry -> analyzeJournalWithAi(entry) },
            onEditClick = { entry -> showAddEditDialog(entry) },
            onDeleteClick = { entry -> confirmDeleteEntry(entry) }
        )
        binding.recyclerJournals.layoutManager = LinearLayoutManager(context)
        binding.recyclerJournals.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnJournalBack.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                (activity as? MainActivity)?.navigateToTab(R.id.nav_dashboard)
            }
        }

        // Direct calm writing save action
        binding.btnSaveJournalDirect.setOnClickListener {
            saveDirectJournalEntry()
        }

        binding.etSearchJournal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterJournals(s?.toString()?.trim() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun saveDirectJournalEntry() {
        val content = binding.etJournalInput.text.toString().trim()
        if (content.isEmpty()) {
            Toast.makeText(requireContext(), "Write a few thoughts first before saving.", Toast.LENGTH_SHORT).show()
            return
        }

        val checkedChipId = binding.chipGroupMood.checkedChipId
        val moodTag = when (checkedChipId) {
            R.id.chipMoodGreat -> "😊 Great"
            R.id.chipMoodGood -> "🙂 Good"
            R.id.chipMoodNeutral -> "😐 Okay"
            R.id.chipMoodLow -> "😟 Low"
            R.id.chipMoodDifficult -> "😣 Difficult"
            else -> "🙂 Good"
        }

        // Automatically extract a natural title from the first sentence or first 6 words
        val firstLine = content.lines().firstOrNull { it.isNotBlank() } ?: content
        val words = firstLine.split(Regex("\\s+"))
        val derivedTitle = if (words.size <= 6) {
            firstLine.take(45)
        } else {
            words.take(6).joinToString(" ") + "..."
        }

        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        lifecycleScope.launch(Dispatchers.IO) {
            db.checkInDao().insertJournal(
                JournalEntity(
                    userId = userEmail,
                    title = derivedTitle,
                    content = content,
                    moodTag = moodTag
                )
            )

            // Update Today's Plan (journalDone = true)
            val plan = db.checkInDao().getDailyPlanByDateSync(userEmail, todayStr) ?: DailyPlanEntity(
                userId = userEmail,
                dateString = todayStr
            )
            db.checkInDao().insertOrUpdateDailyPlan(plan.copy(journalDone = true))

            withContext(Dispatchers.Main) {
                binding.etJournalInput.text.clear()
                Toast.makeText(requireContext(), "Journal reflection saved ✨", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadJournals() {
        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)

        db.checkInDao().getJournalsByUser(userEmail).observe(viewLifecycleOwner) { entries ->
            val b = _binding ?: return@observe
            adapter.submitList(entries)
            b.txtJournalCountBadge.text = "${entries.size} entr${if (entries.size == 1) "y" else "ies"}"
            if (entries.isEmpty()) {
                b.layoutJournalEmpty.visibility = View.VISIBLE
                b.recyclerJournals.visibility = View.GONE
            } else {
                b.layoutJournalEmpty.visibility = View.GONE
                b.recyclerJournals.visibility = View.VISIBLE
            }
        }
    }

    private fun filterJournals(query: String) {
        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)

        if (query.isEmpty()) {
            loadJournals()
        } else {
            db.checkInDao().searchJournals(userEmail, query).observe(viewLifecycleOwner) { filtered ->
                val b = _binding ?: return@observe
                adapter.submitList(filtered)
                b.txtJournalCountBadge.text = "${filtered.size} result${if (filtered.size == 1) "" else "s"}"
                b.layoutJournalEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
                b.recyclerJournals.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun showAddEditDialog(existingEntry: JournalEntity?) {
        val dialogBinding = DialogAddEditJournalBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        if (existingEntry != null) {
            dialogBinding.txtDialogTitle.text = "Edit Reflection"
            dialogBinding.etJournalDialogTitle.setText(existingEntry.title)
            dialogBinding.etJournalDialogContent.setText(existingEntry.content)
            dialogBinding.btnSaveJournal.text = "Update Entry"
        } else {
            dialogBinding.txtDialogTitle.text = "New Reflection"
            dialogBinding.btnSaveJournal.text = "Save Entry"
        }

        dialogBinding.btnCancelJournal.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSaveJournal.setOnClickListener {
            val title = dialogBinding.etJournalDialogTitle.text?.toString()?.trim() ?: ""
            val content = dialogBinding.etJournalDialogContent.text?.toString()?.trim() ?: ""

            if (content.isEmpty()) {
                Toast.makeText(requireContext(), "Please write your thoughts", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val finalTitle = if (title.isNotBlank()) title else "Personal Reflection"
            val checkedChipId = dialogBinding.chipGroupJournalMood.checkedChipId
            val moodTag = if (checkedChipId != View.NO_ID) {
                dialogBinding.root.findViewById<Chip>(checkedChipId)?.text?.toString() ?: "🌿 Calm"
            } else {
                "🌿 Calm"
            }

            val ctx = context?.applicationContext ?: return@setOnClickListener
            val db = MentalCareDatabase.getDatabase(ctx)

            lifecycleScope.launch(Dispatchers.IO) {
                if (existingEntry != null) {
                    val updated = existingEntry.copy(
                        title = finalTitle,
                        content = content,
                        moodTag = moodTag,
                        aiAnalyzed = false // reset for re-analysis
                    )
                    db.checkInDao().updateJournal(updated)
                } else {
                    db.checkInDao().insertJournal(
                        JournalEntity(
                            userId = userEmail,
                            title = finalTitle,
                            content = content,
                            moodTag = moodTag
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), if (existingEntry != null) "Journal updated" else "Journal saved", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun confirmDeleteEntry(entry: JournalEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Journal Entry")
            .setMessage("Are you sure you want to delete '${entry.title}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val ctx = context?.applicationContext ?: return@setPositiveButton
                val db = MentalCareDatabase.getDatabase(ctx)
                lifecycleScope.launch(Dispatchers.IO) {
                    db.checkInDao().deleteJournal(entry)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Journal entry deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun analyzeJournalWithAi(entry: JournalEntity) {
        Toast.makeText(requireContext(), "Analyzing journal with AI...", Toast.LENGTH_SHORT).show()
        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)

        lifecycleScope.launch(Dispatchers.IO) {
            var stressLevel = "LOW"
            var rec = "Your journal reflects a balanced emotional state. Keep writing to build self-awareness."

            try {
                val response = ApiClient.service.analyzeText(TextAnalyzeRequest(text = entry.content))
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    stressLevel = body.stressLevel
                    rec = body.recommendations.firstOrNull() ?: rec
                } else {
                    val local = LocalNLPEngine.analyzeText(entry.content)
                    stressLevel = local.stressLevel
                    rec = local.recommendations.firstOrNull() ?: rec
                }
            } catch (e: Exception) {
                val local = LocalNLPEngine.analyzeText(entry.content)
                stressLevel = local.stressLevel
                rec = local.recommendations.firstOrNull() ?: rec
            }

            val updated = entry.copy(
                aiAnalyzed = true,
                aiStressLevel = stressLevel,
                aiRecommendations = rec
            )
            db.checkInDao().updateJournal(updated)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "AI Analysis: $stressLevel Stress estimated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
