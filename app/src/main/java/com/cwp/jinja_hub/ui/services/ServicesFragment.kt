package com.cwp.jinja_hub.ui.services

import ConsultationModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.ServiceCardAdapter
import com.cwp.jinja_hub.adapters.ServiceCategoryAdapter
import com.cwp.jinja_hub.databinding.FragmentServicesBinding
import com.cwp.jinja_hub.model.CardItem
import com.cwp.jinja_hub.model.ServicesCategory
import com.cwp.jinja_hub.repository.ConsultationRepository
import com.cwp.jinja_hub.repository.ServiceRepository
import com.cwp.jinja_hub.ui.consultation.ConsultationFragment
import kotlinx.coroutines.launch

class ServicesFragment : Fragment() {

    private var _binding: FragmentServicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ServicesViewModel
    private lateinit var categoryAdapter: ServiceCategoryAdapter
    private lateinit var cardAdapter: ServiceCardAdapter

    private var selectedCategory: ServicesCategory? = null
    private val repository: ServiceRepository = ServiceRepository()
    private val consultationRepository: ConsultationRepository = ConsultationRepository()

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
//                navigateToConsultationFragment(
//                    createBundle(
//                        title = selectedCard.title,
//                        imageResId = selectedCard.imageResId,
//                        category = category.name
//                    )
//                )
            },
            onFirstCardClick = { card ->
                //selectedCategory = ServicesCategory(card.id, card.specialty, true)
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
        lifecycleScope.launch {
            val specialists = when (category.specialty) {
                "Therapist" -> consultationRepository.loadProfessionalsFromFirebase(category.specialty)
                "Surgeon" -> consultationRepository.loadProfessionalsFromFirebase(category.specialty)
                else -> emptyList()
            }

            Log.d("ServicesFragment", "Category: ${category.specialty}")

            val bundle = createBundle(
                specialists = ArrayList(specialists),
                category = category.specialty
            )

            // Handling back button press to navigate back to ServicesFragment
            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        // Navigate back to ServicesFragment
                        findNavController().popBackStack()
                    }
                }
            )

            navigateToConsultationFragment(bundle)
        }
    }

    private fun observeCategories() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            Log.d("ServicesFragment", "Categories: $categories") // Log the categories
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
            Log.d("ServicesFragment", "Cards: $cards") // Log the cards
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
