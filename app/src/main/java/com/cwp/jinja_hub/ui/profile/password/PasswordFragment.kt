package com.cwp.jinja_hub.ui.profile.password

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentPasswordBinding
import com.cwp.jinja_hub.ui.client_registration.Login
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import kotlinx.coroutines.launch


class PasswordFragment : Fragment() {

    private lateinit var _binding: FragmentPasswordBinding
    private val binding get() = _binding

    private val viewModel: ProfessionalSignupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Observe progress changes and update a progress bar.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.progress.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
            }
        }

        // Observe error messages and display a Toast when an error occurs.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { errorMsg ->
                    errorMsg?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


        binding.confirm.setOnClickListener {
            if (binding.confirm.text == "Confirm email"){
                val emailInput = binding.email.text.toString()
                viewModel.resetPassword(emailInput) { success, message ->
                    if (success) {
                        binding.layout1.visibility = View.GONE
                        binding.layout2.visibility = View.VISIBLE
                        binding.confirm.text = "Login now"
                        binding.verifyEmail.text = emailInput

                        Toast.makeText(requireContext(), message ?: "Reset email sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), message ?: "Password reset failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Intent(requireContext(), Login::class.java).also {
                    startActivity(it)
                }
            }
        }


    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PasswordFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}