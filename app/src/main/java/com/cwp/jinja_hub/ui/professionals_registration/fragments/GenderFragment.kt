package com.cwp.jinja_hub.ui.professionals_registration.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.professionals_selector.ProfSelectionAdapter
import com.cwp.jinja_hub.ui.client_registration.Login
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignUp


class GenderFragment : Fragment() {

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
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gender, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.optionsRecyclerView)
        val nextButton: Button = view.findViewById(R.id.nextButton)
        val backButton: ImageView = view.findViewById(R.id.backButton)


        // Setup recyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Pass the options and a callback to the adapter
        recyclerView.adapter = ProfSelectionAdapter(options) { selectedOption ->
            selectedOpt.value = selectedOption
            saveSelection(selectedOption)
        }

        backButton.setOnClickListener{
            Intent(requireContext(), Login::class.java).also {
                startActivity(it)
            }
        }


        nextButton.setOnClickListener {
            if (selectedOpt.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please select an option", Toast.LENGTH_SHORT).show()
            } else {
                val current = (activity as? ProfessionalSignUp)?.viewPager?.currentItem ?: 0
                (activity as? ProfessionalSignUp)?.viewPager?.currentItem = current + 1
            }
        }



        return view
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

        fun newInstance(title: String, options: List<String>, preferenceKey: String) =
            GenderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putStringArrayList(ARG_OPTIONS, ArrayList(options))
                    putString(ARG_PREF_KEY, preferenceKey)
                }
            }
    }
}