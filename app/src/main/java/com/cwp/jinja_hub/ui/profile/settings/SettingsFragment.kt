package com.cwp.jinja_hub.ui.profile.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentSettingsBinding
import com.cwp.jinja_hub.ui.profile.settings.fragments.AboutFragment
import com.cwp.jinja_hub.ui.profile.settings.fragments.PolicyFragment
import com.cwp.jinja_hub.ui.profile.settings.fragments.TermsAndConditionFragment


class SettingsFragment : Fragment() {

    private lateinit var _binding : FragmentSettingsBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


        binding.aboutUsLayout.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.settings_container, AboutFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.privacyPolicyLayout.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.settings_container, PolicyFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.termsAndConditionsLayout.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.settings_container, TermsAndConditionFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {

            }
    }
}