package com.cwp.jinja_hub.ui.services

import ConsultationModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
            // Update the selected category and load cards for it
            selectedCategory = category
            viewModel.loadCardsForCategory(category) // Passing the whole category object
        }

        binding.categoryRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }

    private fun setupCardRecyclerView() {
        cardAdapter = ServiceCardAdapter(
            emptyList(),
            ServicesCategory(1, "", false),
            onCardClick = { selectedCard, category ->
                navigateToDetailFragment(selectedCard.title, selectedCard.imageResId, category.name)
            },
            onFirstCardClick = { card ->
                loadSpecialistsForCategory(card)
            }
        )

        binding.cardRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cardAdapter
        }
    }

    private fun loadSpecialistsForCategory(category: CardItem) {
        val specialists = when (category.specialty) {
            "Therapist" -> listOf(
                ConsultationModel("1","Dr. A", "Therapist", R.drawable.profile_image),
                ConsultationModel("2", "Dr. B", "Therapist", R.drawable.profile_image),
                ConsultationModel("3", "Dr. C", "Therapist", R.drawable.profile_image)
            )
            "Surgeon" -> listOf(
                ConsultationModel("1","Dr. X", "Surgeon", R.drawable.profile_image),
                ConsultationModel("2", "Dr. Y", "Surgeon", R.drawable.profile_image),
                ConsultationModel("3","Dr. Z", "Surgeon", R.drawable.profile_image)
            )
            else -> emptyList()
        }

        val bundle = Bundle().apply {
            putParcelableArrayList("specialists", ArrayList(specialists))
            putString("category", category.specialty)
        }

        val consultationFragment = ConsultationFragment().apply {
            arguments = bundle
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.my_container, consultationFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun observeCategories() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            if (categories != null) {
                if (categories.isNotEmpty()) {
                    // Automatically select the first category if none is selected
                    if (selectedCategory == null) {
                        selectedCategory = categories.first()
                        viewModel.loadCardsForCategory(selectedCategory!!) // Use the ServicesCategory object
                    }
                    // Update the adapter with the categories
                    categoryAdapter.updateCategories(categories)
                }
            }
        }
    }

    private fun observeCards() {
        viewModel.cards.observe(viewLifecycleOwner) { cards ->
            cardAdapter.updateCards(cards)
        }
    }

    private fun navigateToDetailFragment(title: String, imageResId: Int, category: String) {
        val bundle = Bundle().apply {
            putString("title", title)
            putInt("imageResId", imageResId)
            putString("category", category)
        }

        val consultationFragment = ConsultationFragment().apply {
            arguments = bundle
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.my_container, consultationFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
