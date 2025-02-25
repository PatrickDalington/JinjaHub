package com.cwp.jinja_hub.ui.professionals_registration.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.professionals_selector.ProfSelectionAdapter
import com.cwp.jinja_hub.ui.client_registration.Login
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignUp
import com.cwp.jinja_hub.ui.profile.settings.fragments.TermsAndConditionFragment


class TAndC : Fragment() {
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_t_and_c, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.optionsRecyclerView)
        val nextButton: Button = view.findViewById(R.id.nextButton)
        val backButton: ImageView = view.findViewById(R.id.backButton)
        val tAndCText: TextView = view.findViewById(R.id.terms_and_condition_text)


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


        tAndCText.setOnClickListener{
            // open the terms and conditions fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.t_and_c_container, TermsAndConditionFragment())
                .addToBackStack(null).commit()
        }


        nextButton.setOnClickListener {
            if (selectedOpt.value.isNullOrEmpty()) {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Friendly Reminder")
                alertDialog.setMessage("We noticed you skipped choosing an option regarding our Terms and Conditions. To move forward, kindly review and select your preference. If you need help, our support team is here.")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.create().show()
            }else if (selectedOpt.value == "No") {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Hello there,")
                alertDialog.setMessage(getString(
                    R.string.t_and_c_message
                ))
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.create().show()
            }else {
                val current = (activity as? ProfessionalSignUp)?.viewPager?.currentItem ?: 0
                (activity as? ProfessionalSignUp)?.viewPager?.currentItem = current + 1
            }
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
            TAndC().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putStringArrayList(ARG_OPTIONS, ArrayList(options))
                    putString(ARG_PREF_KEY, preferenceKey)
                }
            }
    }
}