package com.cwp.jinja_hub.ui.professionals_registration.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.professionals_selector.ProfSelectionAdapter
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignUp

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GenderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfessionFragment : Fragment() {

    private lateinit var title: String
    private lateinit var options: List<String>
    private lateinit var preferenceKey: String

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
        val view = inflater.inflate(R.layout.fragment_profession, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.optionsRecyclerView)
        val nextButton: Button = view.findViewById(R.id.nextButton)
        val backButton: ImageView = view.findViewById(R.id.backButton)

        // Setup recyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Pass the options and a callback to the adapter
        recyclerView.adapter = ProfSelectionAdapter(options) { selectedOption ->
            saveSelection(selectedOption)
        }

        backButton.setOnClickListener{
            (activity as? ProfessionalSignUp)?.viewPager?.currentItem =
                (activity as? ProfessionalSignUp)?.viewPager?.currentItem?.minus(1) ?: 0
        }


        nextButton.setOnClickListener {
            // Go to the next page
            (activity as? ProfessionalSignUp)?.viewPager?.currentItem =
                (activity as? ProfessionalSignUp)?.viewPager?.currentItem?.plus(1) ?: 0
        }



        return view
    }

    private fun saveSelection(selection: String) {
        val sharedPreferences =
            requireContext().getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(preferenceKey, selection).apply()
        Toast.makeText(requireActivity(), "Selection saved: $selection", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_OPTIONS = "options"
        private const val ARG_PREF_KEY = "preferenceKey"

        fun newInstance(title: String, options: List<String>, preferenceKey: String) =
            ProfessionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putStringArrayList(ARG_OPTIONS, ArrayList(options))
                    putString(ARG_PREF_KEY, preferenceKey)
                }
            }
    }
}