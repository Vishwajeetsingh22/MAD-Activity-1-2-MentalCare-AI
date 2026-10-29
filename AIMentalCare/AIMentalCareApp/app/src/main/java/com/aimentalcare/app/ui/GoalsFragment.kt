package com.aimentalcare.app.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aimentalcare.app.R
import com.aimentalcare.app.data.GoalEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.DialogAddGoalBinding
import com.aimentalcare.app.databinding.FragmentGoalsBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GoalsFragment : Fragment() {

    private var _binding: FragmentGoalsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: GoalsAdapter
    private var userEmail: String = "user@mentalcare.ai"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"

        setupRecyclerView()
        setupListeners()
        loadGoals()
    }

    private fun setupRecyclerView() {
        adapter = GoalsAdapter(
            onToggleComplete = { goal, isChecked ->
                toggleGoalCompletion(goal, isChecked)
            },
            onDeleteClick = { goal ->
                confirmDeleteGoal(goal)
            }
        )
        binding.recyclerGoals.layoutManager = LinearLayoutManager(context)
        binding.recyclerGoals.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnGoalsBack.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_dashboard)
        }

        binding.btnAddGoal.setOnClickListener {
            showAddGoalDialog()
        }
    }

    private fun loadGoals() {
        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)

        db.checkInDao().getGoalsByUser(userEmail).observe(viewLifecycleOwner) { goals ->
            val b = _binding ?: return@observe
            adapter.submitList(goals)
            val activeCount = goals.count { !it.isCompleted }
            val completedCount = goals.count { it.isCompleted }

            b.txtGoalsSummary.text = "$activeCount active • $completedCount completed"
            b.layoutGoalsEmpty.visibility = if (goals.isEmpty()) View.VISIBLE else View.GONE
            b.recyclerGoals.visibility = if (goals.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun toggleGoalCompletion(goal: GoalEntity, isCompleted: Boolean) {
        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)

        lifecycleScope.launch(Dispatchers.IO) {
            val updated = goal.copy(isCompleted = isCompleted)
            db.checkInDao().updateGoal(updated)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), if (isCompleted) "Goal marked completed! 🎉" else "Goal in progress", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddGoalDialog() {
        val dialogBinding = DialogAddGoalBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnCancelGoal.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSaveGoal.setOnClickListener {
            val title = dialogBinding.etGoalTitle.text?.toString()?.trim() ?: ""
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a goal description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val checkedChipId = dialogBinding.chipGroupGoalCategory.checkedChipId
            val category = if (checkedChipId != View.NO_ID) {
                dialogBinding.root.findViewById<Chip>(checkedChipId)?.text?.toString() ?: "Wellness"
            } else {
                "Wellness"
            }

            val ctx = context?.applicationContext ?: return@setOnClickListener
            val db = MentalCareDatabase.getDatabase(ctx)

            lifecycleScope.launch(Dispatchers.IO) {
                db.checkInDao().insertGoal(
                    GoalEntity(
                        userId = userEmail,
                        title = title,
                        category = category
                    )
                )
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    Toast.makeText(requireContext(), "Goal added", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun confirmDeleteGoal(goal: GoalEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to remove '${goal.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                val ctx = context?.applicationContext ?: return@setPositiveButton
                val db = MentalCareDatabase.getDatabase(ctx)
                lifecycleScope.launch(Dispatchers.IO) {
                    db.checkInDao().deleteGoal(goal)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Goal removed", Toast.LENGTH_SHORT).show()
                    }
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
