package com.aimentalcare.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.aimentalcare.app.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Expandable Project Structure
        binding.headerProjectStructure.setOnClickListener {
            val isExpanded = binding.txtProjectStructureContent.visibility == View.VISIBLE
            if (isExpanded) {
                binding.txtProjectStructureContent.visibility = View.GONE
                binding.txtToggleStructure.text = "Expand ▼"
            } else {
                binding.txtProjectStructureContent.visibility = View.VISIBLE
                binding.txtToggleStructure.text = "Collapse ▲"
            }
        }

        // Expandable Wellness Disclaimer
        binding.headerDisclaimer.setOnClickListener {
            val isExpanded = binding.txtDisclaimerContent.visibility == View.VISIBLE
            if (isExpanded) {
                binding.txtDisclaimerContent.visibility = View.GONE
                binding.txtToggleDisclaimer.text = "Expand ▼"
            } else {
                binding.txtDisclaimerContent.visibility = View.VISIBLE
                binding.txtToggleDisclaimer.text = "Collapse ▲"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
