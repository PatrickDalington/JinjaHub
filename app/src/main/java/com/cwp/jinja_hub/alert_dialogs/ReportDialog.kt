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
import com.cwp.jinja_hub.databinding.FragmentReportDialogBinding
import com.cwp.jinja_hub.repository.MessageRepository
import com.google.firebase.database.FirebaseDatabase


class ReportDialog : Fragment() {
    private lateinit var _binding: FragmentReportDialogBinding
    private val binding get() = _binding

    var receiverId = ""
    var type = ""
    var category = ""
    var title = ""
    var subTitle = ""
    var spinnerListItems: List<String>? = null
    var productId: String? = null


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
        // Inflate the layout for this fragment
        _binding = FragmentReportDialogBinding.inflate(inflater, container, false)
        // get incoming bundle
        title = arguments?.getString("title").toString()
        subTitle = arguments?.getString("subTitle").toString()
        spinnerListItems = arguments?.getStringArrayList("spinnerList")
        receiverId = arguments?.getString("id").toString()
        type = arguments?.getString("type").toString()
        productId = arguments?.getString("productId")

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        // Header text logic
        headerTextLogic(title, subTitle)

        // Spinner logic
        setUpSpinner(spinnerListItems!!)

        // Send report
        binding.reportButton.setOnClickListener {
            val selectedItemFromSpinner = binding.spinner.selectedItem.toString()
            val moreReason = binding.reason.text.toString()
            sendReport(type, selectedItemFromSpinner, moreReason)
        }

        // back button
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setUpSpinner(spinnerList: List<String>){
        val itemAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, spinnerList)
        itemAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        binding.spinner.adapter = itemAdapter
    }

    private fun headerTextLogic(title: String, subTitle: String){
        if (title == "")
            binding.title.text = "Report"
        else
            binding.title.text = title

        if (subTitle == "")
            binding.subTitle.text = "Let us know what's wrong"
        else
            binding.subTitle.text = subTitle

    }

    private fun validateInput(selectedItemFromSpinner: String, moreReason: String) : Boolean {
        if (selectedItemFromSpinner == "Select report category") {
            Toast.makeText(requireContext(), "Please select report category", Toast.LENGTH_SHORT)
                .show()
            return false
        }

        return true
    }

    private fun sendReport(type: String, selectedItemFromSpinner: String, moreReason: String){
        val isValidated = validateInput(selectedItemFromSpinner, moreReason)
        if (isValidated){
            if (type == "message"){

                messageViewModel.reportUser(
                    type,
                    selectedItemFromSpinner,
                    receiverId,
                    moreReason,
                    ""
                ){
                    if (it){
                        Toast.makeText(requireContext(), "Report sent", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            }else if (type == "market_place"){
                messageViewModel.reportUser(
                    type,
                    selectedItemFromSpinner,
                    receiverId,
                    moreReason,
                    productId!!
                ){
                    if (it){
                        Toast.makeText(requireContext(), "Report sent", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                    else{
                        Toast.makeText(requireContext(), "Report failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }else if (type == "testimonial"){
                messageViewModel.reportUser(
                    type,
                    selectedItemFromSpinner,
                    receiverId,
                    moreReason,
                    productId!!
                ){
                    if (it){
                        Toast.makeText(requireContext(), "Report sent", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ReportDialog().apply {
                arguments = Bundle().apply {

                }
            }
    }
}