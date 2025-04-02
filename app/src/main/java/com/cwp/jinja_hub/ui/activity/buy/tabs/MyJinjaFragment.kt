package com.cwp.jinja_hub.com.cwp.jinja_hub.ui.activity.buy.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.MyADDrinkCardAdapter
import com.cwp.jinja_hub.databinding.FragmentBuyJinjaBinding
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.model.CardItem
import com.cwp.jinja_hub.model.JinjaDrinkCardItem
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.activity.edit.EditDrinkADFragment
import com.cwp.jinja_hub.ui.market_place.ADViewModel

class MyJinjaFragment : Fragment() {

    private var _binding: FragmentBuyJinjaBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ADViewModel
    private lateinit var cardAdapter: MyADDrinkCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyJinjaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModelFactory = ADViewModel.ADViewModelFactory(ADRepository())
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ADViewModel::class.java]

        setupRecyclerView()
        observeViewModel()

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())

        cardAdapter = MyADDrinkCardAdapter(
            listOf(),
            onCardDeleteClick = { card, position ->
                showDeleteConfirmation(card.adId, card.adType, position)
            },
            onCardEditClick = { card, _ ->
                openEditFragment(card)
            },
            viewModel
        )

        binding.recyclerview.adapter = cardAdapter
    }

    private fun observeViewModel() {
        // Observe ads list safely
        viewModel.myAdDrink.observe(viewLifecycleOwner) { cards ->
            if (_binding != null) { // Ensure fragment view still exists
                cardAdapter.updateCards(cards.distinctBy { it.adId })
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        // Observe loading state safely
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (_binding != null) { // Prevent crashes on view destruction
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private fun showDeleteConfirmation(adId: String?, adType: String?, position: Int) {
        if (adId == null || adType == null) return

        AlertDialog.Builder(requireContext())
            .setTitle("Delete Ad")
            .setMessage("Are you sure you want to delete this ad?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteAd(adId, adType) { success ->
                    if (success && _binding != null) {
                        cardAdapter.updateCards(cardAdapter.cards.filterIndexed { index, _ -> index != position })
                    }
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun openEditFragment(card: ADModel) { // Replace with correct model class
        val editFragment = EditDrinkADFragment().apply {
            arguments = Bundle().apply {
                putParcelable("ad_model", card)
            }
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.buy_jinja_drink_container, editFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun refreshData() {
        viewModel.refreshMyDrinkAds()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks and crashes
    }
}
