package com.aimentalcare.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aimentalcare.app.R
import com.aimentalcare.app.data.DailyPlanEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.FragmentDailyPlanBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyPlanFragment : Fragment() {

    private var _binding: FragmentDailyPlanBinding? = null
    private val binding get() = _binding!!

    private var userEmail: String = "user@mentalcare.ai"
    private var todayStr: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireActivity().getSharedPreferences("MentalCarePrefs", android.content.Context.MODE_PRIVATE)
        userEmail = prefs.getString("user_email", "user@mentalcare.ai") ?: "user@mentalcare.ai"
        todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        binding.txtPlanDate.text = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault()).format(Date())

        setupListeners()
        loadPlan()
    }

    private fun setupListeners() {
        binding.btnDailyPlanBack.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_dashboard)
        }

        binding.btnGoToCheckIn.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(DailyCheckInFragment())
        }

        binding.btnGoToBreathing.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_support)
        }

        binding.btnGoToJournal.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(JournalFragment())
        }

        // Card tap handlers for direct toggling
        binding.cardHabitBreathing.setOnClickListener {
            binding.chkPlanBreathing.isChecked = !binding.chkPlanBreathing.isChecked
        }
        binding.cardHabitBreak.setOnClickListener {
            binding.chkPlanBreak.isChecked = !binding.chkPlanBreak.isChecked
        }
        binding.cardHabitReflection.setOnClickListener {
            binding.chkPlanReflection.isChecked = !binding.chkPlanReflection.isChecked
        }
        binding.cardHabitCheckIn.setOnClickListener {
            binding.chkPlanCheckIn.isChecked = !binding.chkPlanCheckIn.isChecked
        }
        binding.cardHabitJournal.setOnClickListener {
            binding.chkPlanJournal.isChecked = !binding.chkPlanJournal.isChecked
        }

        // Checkbox listeners
        binding.chkPlanBreak.setOnCheckedChangeListener { _, isChecked ->
            updateStatusPill(binding.txtStatusBreak, isChecked)
            updatePlanItem { it.copy(breakDone = isChecked) }
        }

        binding.chkPlanReflection.setOnCheckedChangeListener { _, isChecked ->
            updateStatusPill(binding.txtStatusReflection, isChecked)
            updatePlanItem { it.copy(reflectionDone = isChecked) }
        }

        binding.chkPlanCheckIn.setOnCheckedChangeListener { _, isChecked ->
            updateStatusPill(binding.txtStatusCheckIn, isChecked)
            updatePlanItem { it.copy(checkInDone = isChecked) }
        }

        binding.chkPlanBreathing.setOnCheckedChangeListener { _, isChecked ->
            updateStatusPill(binding.txtStatusBreathing, isChecked)
            updatePlanItem { it.copy(breathingDone = isChecked) }
        }

        binding.chkPlanJournal.setOnCheckedChangeListener { _, isChecked ->
            updateStatusPill(binding.txtStatusJournal, isChecked)
            updatePlanItem { it.copy(journalDone = isChecked) }
        }
    }

    private fun updateStatusPill(pillView: TextView, isCompleted: Boolean) {
        val ctx = context ?: return
        if (isCompleted) {
            pillView.text = "✓ Completed"
            pillView.setTextColor(ContextCompat.getColor(ctx, R.color.primary_green))
            pillView.setBackgroundResource(R.drawable.bg_pill_completed)
        } else {
            pillView.text = "○ Pending"
            pillView.setTextColor(ContextCompat.getColor(ctx, R.color.secondary_text))
            pillView.setBackgroundResource(R.drawable.bg_pill_pending)
        }
    }

    private fun loadPlan() {
        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)

        db.checkInDao().getDailyPlanByDate(userEmail, todayStr).observe(viewLifecycleOwner) { plan ->
            val b = _binding ?: return@observe
            val p = plan ?: DailyPlanEntity(userId = userEmail, dateString = todayStr)

            b.chkPlanCheckIn.isChecked = p.checkInDone
            b.chkPlanBreathing.isChecked = p.breathingDone
            b.chkPlanJournal.isChecked = p.journalDone
            b.chkPlanBreak.isChecked = p.breakDone
            b.chkPlanReflection.isChecked = p.reflectionDone

            updateStatusPill(b.txtStatusCheckIn, p.checkInDone)
            updateStatusPill(b.txtStatusBreathing, p.breathingDone)
            updateStatusPill(b.txtStatusJournal, p.journalDone)
            updateStatusPill(b.txtStatusBreak, p.breakDone)
            updateStatusPill(b.txtStatusReflection, p.reflectionDone)

            val completed = p.completedCount()
            b.txtDailyProgressCount.text = "$completed / 5 completed"
            b.progressDailyPlan.progress = completed
        }
    }

    private fun updatePlanItem(updateFn: (DailyPlanEntity) -> DailyPlanEntity) {
        val ctx = context?.applicationContext ?: return
        val db = MentalCareDatabase.getDatabase(ctx)

        lifecycleScope.launch(Dispatchers.IO) {
            val current = db.checkInDao().getDailyPlanByDateSync(userEmail, todayStr) ?: DailyPlanEntity(
                userId = userEmail,
                dateString = todayStr
            )
            val updated = updateFn(current)
            db.checkInDao().insertOrUpdateDailyPlan(updated)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
