package com.cwp.jinja_hub.ui.profile.settings.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.cwp.jinja_hub.databinding.FragmentTermsAndConditionBinding


class TermsAndConditionFragment : Fragment() {
    private lateinit var _binding: FragmentTermsAndConditionBinding
    private val binding get() = _binding

    private lateinit var title: String
    private lateinit var options: List<String>
    private lateinit var preferenceKey: String

    private val selectedOpt = MutableLiveData<String?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE, "")
            options = it.getStringArrayList(ARG_OPTIONS) ?: emptyList()
            preferenceKey = it.getString(ARG_PREF_KEY, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsAndConditionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.back.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

    }


    private fun saveSelection(selection: String) {
        val sharedPreferences =
            requireContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(preferenceKey, selection).apply()
    }



    companion object {

        private const val ARG_TITLE = "title"
        private const val ARG_OPTIONS = "options"
        private const val ARG_PREF_KEY = "preferenceKey"

        @JvmStatic
        fun newInstance(title: String, options: List<String>, preferenceKey: String) =
            TermsAndConditionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putStringArrayList(ARG_OPTIONS, ArrayList(options))
                    putString(ARG_PREF_KEY, preferenceKey)
                }
            }
    }
}