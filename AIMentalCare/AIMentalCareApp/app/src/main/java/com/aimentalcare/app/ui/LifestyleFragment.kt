package com.aimentalcare.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.aimentalcare.app.R
import com.aimentalcare.app.data.LifestyleEntity
import com.aimentalcare.app.data.MentalCareDatabase
import com.aimentalcare.app.databinding.FragmentLifestyleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LifestyleFragment : Fragment() {

    private var _binding: FragmentLifestyleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLifestyleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogLifestyle.setOnClickListener {
            val sleepStr = binding.etSleepDuration.text.toString().trim()
            val workStr = binding.etWorkHours.text.toString().trim()
            val screenStr = binding.etScreenTime.text.toString().trim()
            val exStr = binding.etExerciseMins.text.toString().trim()

            val sleep = sleepStr.toFloatOrNull() ?: 6.2f
            val work = workStr.toFloatOrNull() ?: 9.0f
            val screen = screenStr.toFloatOrNull() ?: 6.5f
            val ex = exStr.toIntOrNull() ?: 15

            calculateLifestyleRisk(sleep, work, screen, ex)
        }
    }

    private fun calculateLifestyleRisk(sleep: Float, work: Float, screen: Float, ex: Int) {
        val appCtx = context?.applicationContext ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            var score = 40.0
            if (sleep < 6.0f) score += (6.0f - sleep) * 12.0
            if (work > 8.0f) score += (work - 8.0f) * 7.0
            if (screen > 7.0f) score += (screen - 7.0f) * 4.0
            if (ex < 20) score += 10.0 else score -= 8.0

            val finalScore = score.coerceIn(15.0, 95.0).toInt()

            val db = MentalCareDatabase.getDatabase(appCtx)
            db.checkInDao().insertLifestyle(
                LifestyleEntity(
                    sleepHrs = sleep,
                    workHrs = work,
                    screenHrs = screen,
                    exerciseMins = ex,
                    energyLevel = if (sleep < 6.0f) "Low" else "Medium",
                    moodEmoji = if (finalScore > 70) "😫" else "😊",
                    stressScore = finalScore
                )
            )

            withContext(Dispatchers.Main) {
                val b = _binding ?: return@withContext

                b.cardLifestyleResult.visibility = View.VISIBLE
                b.txtLifestyleScore.text = getString(R.string.lifestyle_risk_format, finalScore)

                val factors = StringBuilder()
                if (sleep < 6.0f) factors.append(getString(R.string.low_sleep_factor, sleep)).append("\n")
                if (work > 8.0f) factors.append(getString(R.string.high_work_factor, work)).append("\n")
                if (ex < 20) factors.append(getString(R.string.low_exercise_factor, ex)).append("\n")
                if (factors.isEmpty()) factors.append(getString(R.string.healthy_lifestyle))

                b.txtLifestyleFactors.text = factors.toString().trim()
                Toast.makeText(appCtx, R.string.lifestyle_logged, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
