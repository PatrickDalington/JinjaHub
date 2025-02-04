package com.cwp.jinja_hub.ui.consultation

import ConsultationModel
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.adapters.ConsultationAdapter
import com.cwp.jinja_hub.databinding.FragmentConsultationBinding
import com.cwp.jinja_hub.repository.ConsultationRepository
import com.cwp.jinja_hub.utils.NavigateTo

class ConsultationFragment : Fragment() {
    private var _binding: FragmentConsultationBinding? = null
    private val binding get() = _binding!!

    private lateinit var consultationAdapter: ConsultationAdapter
    private lateinit var viewModel: ConsultationViewModel

    private lateinit var specialists: List<ConsultationModel>
    private lateinit var category: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConsultationBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel with the factory
        viewModel = ViewModelProvider(this, ConsultationViewModelFactory(ConsultationRepository()))[ConsultationViewModel::class.java]

        // Setup RecyclerView
        setupRecyclerView()

        // Get category from the arguments and load specialists
        // Retrieve the arguments passed from the ServiceFragment
        arguments?.let {
            specialists = it.getParcelableArrayList("specialists", ConsultationModel::class.java) ?: emptyList()
            category = it.getString("category", "")
        }

        binding.detailTitle.text = "Specialists for $category"
        viewModel.loadSpecialistsForCategory(category)

        // get info of specialist



        // Navigate back to ServicesFragment
        binding.arrowBack.setOnClickListener {
            NavigateTo().navigateToServices(findNavController())
        }

        // Observe consultation state
        observeConsultationState()
    }

    private fun setupRecyclerView() {
        consultationAdapter = ConsultationAdapter(emptyList())  // Initialize with an empty list
        binding.consultationRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = consultationAdapter
        }
    }

    private fun observeConsultationState() {
        viewModel.consultationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                ConsultationViewModel.ConsultationState.Loading -> showLoading(true)
                is ConsultationViewModel.ConsultationState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                is ConsultationViewModel.ConsultationState.Success -> {
                    showLoading(false)
                    consultationAdapter.updateConsultations(state.consultations)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

        // Optionally add a retry button
        binding.retryButton.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                visibility = View.GONE
                val category = arguments?.getString("category") ?: ""
                viewModel.loadSpecialistsForCategory(category)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// ViewModel Factory to provide the repository instance to the ViewModel
class ConsultationViewModelFactory(private val consultationRepository: ConsultationRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConsultationViewModel(consultationRepository) as T
    }
}
