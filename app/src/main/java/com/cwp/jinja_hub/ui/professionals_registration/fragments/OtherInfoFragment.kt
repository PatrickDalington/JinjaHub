package com.cwp.jinja_hub.ui.professionals_registration.fragments

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.adapters.professionals_selector.ProfSelectionAdapter
import com.cwp.jinja_hub.databinding.FragmentOtherInfoBinding
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignUp
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import com.cwp.jinja_hub.ui.welcome.Welcome
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OtherInfoFragment : Fragment() {

    private var _binding: FragmentOtherInfoBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share data with other fragments in the same activity.
    private val viewModel: ProfessionalSignupViewModel by activityViewModels()
    private val personalInfoViewModel: PersonalInfoViewModel by activityViewModels()

    private lateinit var title: String
    private lateinit var options: List<String>
    private lateinit var preferenceKey: String

    private var optionSelected: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE, "")
            options = it.getStringArrayList(ARG_OPTIONS) ?: emptyList()
            preferenceKey = it.getString(ARG_PREF_KEY, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtherInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup options RecyclerView using view binding.
        binding.optionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.optionsRecyclerView.adapter = ProfSelectionAdapter(options) { selectedOption ->
            optionSelected = selectedOption
        }

        // Setup the back button (assuming your layout has a backButton).
        binding.backButton.setOnClickListener {
            // Navigate back in your ProfessionalSignUp's ViewPager.
            (activity as? ProfessionalSignUp)?.viewPager?.currentItem =
                ((activity as? ProfessionalSignUp)?.viewPager?.currentItem ?: 0) - 1
        }

        // Submit button handling: load user info from shared preferences and update the form in the ViewModel.
        binding.submit.setOnClickListener {
            personalInfoViewModel.loadInfoFromSharedPreferences(requireContext()) { fullName, username, email, password ->
                // Safely split full name into first and last names.
                val nameParts = fullName.trim().split(" ")
                val firstName = nameParts.firstOrNull() ?: ""
                val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else "N/A"

                // Update the form data in ProfessionalSignupViewModel.
                viewModel.updateForm {
                    it.fullName = fullName
                    it.firstName = firstName
                    it.lastName = lastName
                    it.username = username
                    it.email = email
                    it.password = password
                    it.gender = getPreferenceValue("gender")
                    it.age = getPreferenceValue("age")
                    it.address = ""
                    it.workplace = ""
                    it.medicalProfessional = optionSelected
                    it.licence = ""
                    it.yearsOfWork = ""
                    it.consultationTime = ""
                }

                // Call signup.
                viewModel.signup(requireActivity())
            }
        }

        // Observe signup success.
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.isSignupSuccessful.collectLatest { isSuccessful ->
                if (isSuccessful) {
                    showSignupSuccessDialog()
                } else {
                    Toast.makeText(requireActivity(), "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe progress.
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.progress.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observe error messages.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.errorMessage.collectLatest { errorMessage ->
                    errorMessage?.let {
                        AlertDialog.Builder(requireActivity()).apply {
                            setTitle("Error")
                            setMessage(it)
                            setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                        }.create().show()
                    }
                }
            }
        }
    }

    private fun showSignupSuccessDialog() {
        val userId = viewModel.userId.value
        AlertDialog.Builder(requireActivity()).apply {
            setTitle("Hey, ${viewModel.formData.value.fullName}")
            setMessage("We have sent you a verification email. Please open your email app to verify your email.")
            setPositiveButton("Open Email App") { _, _ ->
                // Try to open the Gmail app by its package name.
                val packageManager = requireActivity().packageManager
                val gmailIntent = packageManager.getLaunchIntentForPackage("com.google.android.gm")
                if (gmailIntent != null) {
                    startActivity(gmailIntent)
                } else {
                    // Fallback: open a chooser for email apps if Gmail is not installed.
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                    startActivity(Intent.createChooser(intent, "Choose an email app"))
                }
            }
            setNegativeButton("Skip") { dialog, _ ->
                val intent = Intent(requireActivity(), Welcome::class.java).apply {
                    putExtra("firstName", viewModel.formData.value.fullName.split(" ").firstOrNull())
                    putExtra("userId", userId)
                }
                startActivity(intent)
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun getPreferenceValue(key: String): String {
        return requireActivity().getSharedPreferences("user_preferences", MODE_PRIVATE)
            .getString(key, "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_OPTIONS = "options"
        private const val ARG_PREF_KEY = "preferenceKey"
        fun newInstance(title: String, options: List<String>, preferenceKey: String) =
            OtherInfoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putStringArrayList(ARG_OPTIONS, ArrayList(options))
                    putString(ARG_PREF_KEY, preferenceKey)
                }
            }
    }
}
