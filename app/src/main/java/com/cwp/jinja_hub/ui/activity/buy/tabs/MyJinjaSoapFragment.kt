package com.cwp.jinja_hub.com.cwp.jinja_hub.ui.activity.buy.tabs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.com.cwp.jinja_hub.adapters.MyADSoapCardAdapter
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.activity.edit.EditSoapADFragment
import com.cwp.jinja_hub.databinding.FragmentBuyJinjaSoapBinding
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.activity.edit.EditDrinkADFragment
import com.cwp.jinja_hub.ui.market_place.ADViewModel
import com.cwp.jinja_hub.ui.market_place.details.ProductDetail

class MyJinjaSoapFragment : Fragment() {

    private lateinit var viewModel: ADViewModel
    private lateinit var cardAdapter: MyADSoapCardAdapter

    private val repository = ADRepository()

    private var _binding: FragmentBuyJinjaSoapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyJinjaSoapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // Initialize the ViewModel
        val viewModelFactory = ADViewModel.ADViewModelFactory(ADRepository())
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ADViewModel::class.java]

        // Set up the adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        cardAdapter = MyADSoapCardAdapter(
            listOf(), // Initialize with an empty list
            onCardDeleteClick = { card, position ->
                // create alert to make sure user wants to delete
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Delete Ad")
                alertDialog.setMessage("Are you sure you want to delete this ad?")
                alertDialog.setPositiveButton("Yes") { _, _ ->
                    card.adId?.let { adId ->
                        viewModel.deleteAd(adId, card.adType) { success ->
                            if (success) {
                                viewModel.deleteAd(
                                    adId,
                                    card.adType
                                ) {
                                    if (it) {
                                        cardAdapter.notifyItemRemoved(position)
                                        cardAdapter.notifyItemRangeChanged(position, cardAdapter.itemCount)
                                    }
                                }
                                cardAdapter.updateCards(cardAdapter.cards.filterIndexed { index, _ -> index != position })
                            }
                        }
                    }
                }
                alertDialog.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.create().show()
            },
            onCardEditClick = { card, position ->
                // Open EditDrinkADFragment with the selected card
                val editFragment = EditSoapADFragment()
                val bundle = Bundle().apply {
                    putParcelable("ad_model", card)
                }
                editFragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.buy_jinja_soap_container, editFragment)
                    .addToBackStack(null)
                    .commit()
            },
            repository
        )


//        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
//            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
//        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        binding.recyclerview.adapter = cardAdapter

        // Observe the ViewModel for updates
        viewModel.myAdSoap.observe(viewLifecycleOwner) { cards ->
            cardAdapter.updateCards(cards)
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun refreshData() {
        viewModel.refreshMySoapAds() // Fetch new data from ViewModel
    }

}
