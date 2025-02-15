package com.cwp.jinja_hub.ui.profile.contact_us.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentContactUsMessageBinding
import com.cwp.jinja_hub.ui.profile.ContactUsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class ContactUsMessageFragment : Fragment() {

    private lateinit var _binding: FragmentContactUsMessageBinding
    private val binding get() = _binding

    private lateinit var viewModel: ContactUsViewModel

    private lateinit var fUser: FirebaseUser

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
        _binding = FragmentContactUsMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fUser = FirebaseAuth.getInstance().currentUser!!

        viewModel = ViewModelProvider(this).get(ContactUsViewModel::class.java)

        val customerCare = listOf(
            R.drawable.customer_care_1,
            R.drawable.customer_care_2,
            R.drawable.customer_care_3
        ).random()

        binding.customerCareImage.setImageResource(customerCare)

        // List of spinner options
        val options = listOf(
            "General Inquiry",
            "Billing & Payments",
            "Account & Login Issues",
            "Orders & Transactions",
            "Product & Service Inquiries",
            "Others")

        // Setup spinner with default dropdown adapter



        binding.send.setOnClickListener {
            val subject = binding.subject.text.toString().trim()
            val message = binding.message.text.toString().trim()
            // Assume you have a user ID (this might come from FirebaseAuth or your app's logic).

            // Call the sendMessage() function in the ViewModel.
            viewModel.sendMessage(fUser.uid, subject, message) { success ->
                if (success) {
                    clearViews()
                    parentFragmentManager.popBackStack()
                    Toast.makeText(requireContext(), "Message sent successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.backButton.setOnClickListener {
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
    }

    private fun clearViews(){
        binding.message.text.clear()
        binding.subject.text.clear()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContactUsMessageFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}