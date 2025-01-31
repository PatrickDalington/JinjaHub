package com.cwp.jinja_hub.ui.services

import ConsultationModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.ServiceCardAdapter
import com.cwp.jinja_hub.adapters.ServiceCategoryAdapter
import com.cwp.jinja_hub.databinding.FragmentServicesBinding
import com.cwp.jinja_hub.model.CardItem
import com.cwp.jinja_hub.model.ServicesCategory
import com.cwp.jinja_hub.repository.ServiceRepository
import com.cwp.jinja_hub.ui.consultation.ConsultationFragment

class ServicesFragment : Fragment() {

    private var _binding: FragmentServicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ServicesViewModel
    private lateinit var categoryAdapter: ServiceCategoryAdapter
    private lateinit var cardAdapter: ServiceCardAdapter

    private var selectedCategory: ServicesCategory? = null
    private val repository: ServiceRepository = ServiceRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val viewModelFactory = ServicesViewModel.ServicesViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[ServicesViewModel::class.java]

        // Set up RecyclerViews
        setupCategoryRecyclerView()
        setupCardRecyclerView()

        // Observe ViewModel data
        observeCategories()
        observeCards()

        // Load initial data
        viewModel.loadInitialCategories()
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = ServiceCategoryAdapter(emptyList()) { category ->
            selectedCategory = category
            viewModel.loadCardsForCategory(category)
        }

        binding.categoryRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupCardRecyclerView() {
        cardAdapter = ServiceCardAdapter(
            emptyList(),
            ServicesCategory(1, "", false),
            onCardClick = { selectedCard, category ->
                navigateToConsultationFragment(
                    createBundle(
                        title = selectedCard.title,
                        imageResId = selectedCard.imageResId,
                        category = category.name
                    )
                )
            },
            onFirstCardClick = { card ->
                loadSpecialistsForCategory(card)
            }
        )

        binding.cardRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cardAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadSpecialistsForCategory(category: CardItem) {
        val specialists = when (category.specialty) {
            "Therapist" -> listOf(
                ConsultationModel("1", "Dr. A", "Therapist", R.drawable.profile_image),
                ConsultationModel("2", "Dr. B", "Therapist", R.drawable.profile_image),
                ConsultationModel("3", "Dr. C", "Therapist", R.drawable.profile_image)
            )
            "Surgeon" -> listOf(
                ConsultationModel("1", "Dr. X", "Surgeon", R.drawable.profile_image),
                ConsultationModel("2", "Dr. Y", "Surgeon", R.drawable.profile_image),
                ConsultationModel("3", "Dr. Z", "Surgeon", R.drawable.profile_image)
            )
            else -> emptyList()
        }

        val bundle = createBundle(
            specialists = ArrayList(specialists),
            category = category.specialty
        )

        // Handling back button press to navigate back to ServicesFragment
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Navigate back to HomeFragment
                    findNavController().navigate(R.id.navigation_services)
                }
            }
        )


        navigateToConsultationFragment(bundle)
    }

    private fun observeCategories() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            if (!categories.isNullOrEmpty()) {
                // Automatically select the first category if none is selected
                if (selectedCategory == null) {
                    selectedCategory = categories.first()
                    viewModel.loadCardsForCategory(selectedCategory!!)
                }
                categoryAdapter.updateCategories(categories)
            } else {
                showEmptyState(true)
            }
        }
    }

    private fun observeCards() {
        viewModel.cards.observe(viewLifecycleOwner) { cards ->
            if (cards.isNotEmpty()) {
                cardAdapter.updateCards(cards)
                showEmptyState(false)
            } else {
                showEmptyState(true)
            }
        }
    }

    private fun createBundle(
        title: String? = null,
        imageResId: Int? = null,
        category: String? = null,
        specialists: ArrayList<ConsultationModel>? = null
    ): Bundle {
        return Bundle().apply {
            title?.let { putString("title", it) }
            imageResId?.let { putInt("imageResId", it) }
            category?.let { putString("category", it) }
            specialists?.let { putParcelableArrayList("specialists", it) }
        }
    }

    private fun navigateToConsultationFragment(bundle: Bundle) {
        val consultationFragment = ConsultationFragment().apply {
            arguments = bundle
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.my_container, consultationFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showEmptyState(show: Boolean) {
        binding.emptyStateContainer.visibility = if (show) View.VISIBLE else View.GONE
        binding.cardRecycler.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
