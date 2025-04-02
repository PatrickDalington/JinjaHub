package com.cwp.jinja_hub.alert_dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.message.MessageViewModel
import com.cwp.jinja_hub.databinding.FragmentReportBottomSheetBinding
import com.cwp.jinja_hub.repository.MessageRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.FirebaseDatabase

class ReportBottomSheet : BottomSheetDialogFragment() {
    private lateinit var _binding: FragmentReportBottomSheetBinding
    private val binding get() = _binding

    var receiverId = ""
    var type = ""
    var category = ""
    var title = ""
    private var subTitle = ""
    private var spinnerListItems: List<String>? = null
    private var productId: String? = null // productId is nullable

    private lateinit var messageViewModel: MessageViewModel
    private val firebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = MessageRepository(firebaseDatabase)
        messageViewModel = ViewModelProvider(
            this,
            MessageViewModel.MessageViewModelFactory(repository)
        )[MessageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBottomSheetBinding.inflate(inflater, container, false)

        // Get incoming bundle
        title = arguments?.getString("title").orEmpty()  // Use orEmpty() to avoid null values
        subTitle = arguments?.getString("subTitle").orEmpty()
        spinnerListItems = arguments?.getStringArrayList("spinnerList")
        receiverId = arguments?.getString("id").orEmpty()
        type = arguments?.getString("type").orEmpty()
        productId = arguments?.getString("productId") // productId may still be null here

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Header text logic
        headerTextLogic(title, subTitle)

        // Spinner logic
        setUpSpinner(spinnerListItems ?: emptyList())  // Handle null safely

        // Send report
        binding.reportButton.setOnClickListener {
            val selectedItemFromSpinner = binding.spinner.selectedItem.toString()
            val moreReason = binding.reason.text.toString()
            sendReport(type, selectedItemFromSpinner, moreReason)
        }

        // Back button
        binding.backButton.setOnClickListener {
            dismiss()
        }
    }

    private fun setUpSpinner(spinnerList: List<String>){
        val itemAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, spinnerList)
        itemAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        binding.spinner.adapter = itemAdapter
    }

    private fun headerTextLogic(title: String, subTitle: String){
        binding.title.text = if (title.isEmpty()) "Report" else title
        binding.subTitle.text = if (subTitle.isEmpty()) "Let us know what's wrong" else subTitle
    }

    private fun validateInput(selectedItemFromSpinner: String, moreReason: String) : Boolean {
        if (selectedItemFromSpinner == "Select report category") {
            Toast.makeText(requireContext(), "Please select report category", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun sendReport(type: String, selectedItemFromSpinner: String, moreReason: String) {
        val isValidated = validateInput(selectedItemFromSpinner, moreReason)
        if (isValidated) {
            val validProductId = productId ?: ""  // Provide a fallback empty string or another fallback if needed
            if (type == "message") {
                messageViewModel.reportUser(
                    type,
                    selectedItemFromSpinner,
                    receiverId,
                    moreReason,
                    validProductId
                ) { success ->
                    handleReportResult(success)
                }
            } else if (type == "market_place") {
                messageViewModel.reportUser(
                    type,
                    selectedItemFromSpinner,
                    receiverId,
                    moreReason,
                    validProductId
                ) { success ->
                    handleReportResult(success)
                }
            } else if (type == "testimonial") {
                messageViewModel.reportUser(
                    type,
                    selectedItemFromSpinner,
                    receiverId,
                    moreReason,
                    validProductId
                ) { success ->
                    handleReportResult(success)
                }
            }
        }
    }

    private fun handleReportResult(success: Boolean) {
        if (success) {
            Toast.makeText(requireContext(), "Report sent", Toast.LENGTH_SHORT).show()
            dismiss()
        } else {
            Toast.makeText(requireContext(), "Report failed", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReportBottomSheet().apply {
                arguments = Bundle().apply {
                    // Populate bundle if needed
                }
            }
    }
}
