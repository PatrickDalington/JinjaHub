package com.cwp.jinja_hub.ui.professionals_registration.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.cwp.jinja_hub.databinding.FragmentPersonalInfoBinding
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignUp
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class PersonalInfoFragment : Fragment() {
    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PersonalInfoViewModel by activityViewModels()


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextButton.setOnClickListener {
            // Get all the text entered in the EditText fields
            val fullName = binding.fullName.text.toString().trim()
            val username = binding.username.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            // Check if any field is empty
            if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(requireActivity(), "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Set values in ViewModel (triggers validation)
            viewModel.setFullName(fullName)
            viewModel.setUsername(username)
            viewModel.setEmail(email)
            viewModel.setPassword(password)

            // Check for validation errors before proceeding
            if (viewModel.usernameError.value != null ||
                viewModel.emailError.value != null ||
                viewModel.passwordError.value != null) {

                // Show error messages if any validation fails
                viewModel.usernameError.value?.let {
                    binding.username.error = it
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }

                viewModel.emailError.value?.let {
                    binding.email.error = it
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }
                viewModel.passwordError.value?.let {
                    binding.password.error = it
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }

                return@setOnClickListener  // **Stop further execution**
            }

            // Save validated data
            viewModel.saveInfoToSharedPreferences(requireContext())
        }

        binding.backButton.setOnClickListener {
            (activity as? ProfessionalSignUp)?.viewPager?.currentItem =
                (activity as? ProfessionalSignUp)?.viewPager?.currentItem?.minus(1) ?: 0
        }

        // Observe save success and navigate to next fragment
        viewModel.isSavedSuccessful.observe(viewLifecycleOwner) { isSuccessful ->
            if (isSuccessful) {
                viewModel.loadInfoFromSharedPreferences(requireContext()) { fullName, username, email, password ->
                    Toast.makeText(requireActivity(), "$fullName, $username, $email, $password", Toast.LENGTH_SHORT).show()
                    (activity as? ProfessionalSignUp)?.viewPager?.currentItem =
                        (activity as? ProfessionalSignUp)?.viewPager?.currentItem?.plus(1) ?: 0
                }
            } else {
                Toast.makeText(requireActivity(), "Failed to save!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_OPTIONS = "options"
        private const val ARG_PREF_KEY = "preferenceKey"

        fun newInstance(title: String, options: List<String>, preferenceKey: String) =
            PersonalInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    // Log the title received
                    Log.d("GenderFragment", "Title received: $title")
                    // Log the options received
                    Log.d("GenderFragment", "Options received: $options")
                    putStringArrayList(ARG_OPTIONS, ArrayList(options))
                    putString(ARG_PREF_KEY, preferenceKey)
                }
            }
    }
}