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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cwp.jinja_hub.databinding.FragmentOtherInfoBinding
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignUp
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import com.cwp.jinja_hub.ui.welcome.Welcome
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OtherInfoFragment : Fragment() {

    private var _binding: FragmentOtherInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfessionalSignupViewModel by activityViewModels()
    private val personalInfoViewModel: PersonalInfoViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtherInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener{
            (activity as? ProfessionalSignUp)?.viewPager?.currentItem =
                (activity as? ProfessionalSignUp)?.viewPager?.currentItem?.minus(1) ?: 0
        }

        binding.submit.setOnClickListener {
            personalInfoViewModel.loadInfoFromSharedPreferences(requireContext()) { fullName, username, email, password ->
                val address = binding.address.text.toString().trim()
                val workplace = binding.workPlace.text.toString().trim()
                val licence = binding.licence.text.toString().trim()
                val yearsOfWork = binding.yearsOfWork.text.toString().trim()
                val consultationTime = binding.consultTime.text.toString().trim()

                if (listOf(address, workplace, licence, yearsOfWork, consultationTime).any { it.isBlank() }) {
                    Toast.makeText(requireActivity(), "All fields are required!", Toast.LENGTH_SHORT).show()
                    return@loadInfoFromSharedPreferences
                }

                // Splitting full name safely
                val nameParts = fullName.trim().split(" ")
                val firstName = nameParts.firstOrNull() ?: ""
                val lastName = nameParts.drop(1).joinToString(" ").ifEmpty { "N/A" }

                // Update ViewModel with form data
                viewModel.updateForm {
                    it.fullName = fullName
                    it.firstName = firstName
                    it.lastName = lastName
                    it.username = username
                    it.email = email
                    it.password = password
                    it.gender = getPreferenceValue("gender")
                    it.age = getPreferenceValue("age")
                    it.address = address
                    it.workplace = workplace
                    it.profession = getPreferenceValue("profession")
                    it.licence = licence
                    it.yearsOfWork = yearsOfWork
                    it.consultationTime = consultationTime
                }

                viewModel.signup(requireActivity())
            }
        }

        // Observe Signup Success
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.isSignupSuccessful.collectLatest { isSuccessful ->
                if (isSuccessful) showSignupSuccessDialog()
                else Toast.makeText(requireActivity(), "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe Progress
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.progress.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        // Observe Error Messages
        // Observe Error Messages (StateFlow)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collectLatest { errorMessage ->
                    errorMessage?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun showSignupSuccessDialog() {
        val userId = viewModel.userId.value
        AlertDialog.Builder(requireActivity()).apply {
            setTitle("Hey, ${viewModel.formData.value.fullName}")
            setMessage("We have sent you a verification email. Please verify your email.")
            setPositiveButton("OK") { _, _ ->
                val intent = Intent(requireActivity(), Welcome::class.java).apply {
                    putExtra("firstName", viewModel.formData.value.fullName.split(" ").getOrNull(0))
                    putExtra("userId", userId)
                }
                startActivity(intent)
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
        fun newInstance(title: String, options: List<String>, preferenceKey: String) =
            OtherInfoFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putStringArrayList("options", ArrayList(options))
                    putString("preferenceKey", preferenceKey)
                }
            }
    }
}
