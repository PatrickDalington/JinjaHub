package com.cwp.jinja_hub.ui.market_place.buy.tabs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.adapters.AdPlaceholder
import com.cwp.jinja_hub.adapters.JinjaSoapCardAdapter
import com.cwp.jinja_hub.databinding.FragmentBuyJinjaSoapBinding
import com.cwp.jinja_hub.helpers.SendRegularNotification
import com.cwp.jinja_hub.model.NotificationModel
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.market_place.ADViewModel
import com.cwp.jinja_hub.ui.market_place.details.ProductDetail
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.google.firebase.auth.FirebaseAuth

class BuyJinjaSoapFragment : Fragment() {

    private lateinit var viewModel: ADViewModel
    private lateinit var cardAdapter: JinjaSoapCardAdapter

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



        viewModel.refreshMySoapAds()


        parentFragmentManager.setFragmentResultListener("filterRequestKey", viewLifecycleOwner) { _, bundle ->
            val country = bundle.getString("selected_country", "")
            val state = bundle.getString("selected_state", "")
            val area = bundle.getString("selected_area", "")

            if (country.isNotEmpty()) {
                filterAds(country, state, area)
            } else {
                Toast.makeText(requireContext(), "Please select a country to filter.", Toast.LENGTH_SHORT).show()
            }
        }




        // Back pressed
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, true) {
            val fragmentManager = requireActivity().supportFragmentManager
            if (fragmentManager.backStackEntryCount > 0) {
                fragmentManager.popBackStack()
            } else {
                requireActivity().finish() // Close only if no more fragments in stack
            }
        }


        // Set up the adapter
        binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        cardAdapter = JinjaSoapCardAdapter(
            listOf(), // Initialize with an empty list
            onMessageClick = { card ->
                if (card.posterId != FirebaseAuth.getInstance().currentUser?.uid) {
                    Intent(requireActivity(), MessageActivity::class.java).apply {
                        putExtra("receiverId", card.posterId)
                        startActivity(this)
                    }
                    val senNotification = SendRegularNotification()
                    val notification = NotificationModel(
                        posterId = card.posterId ?: "",
                        content = "Someone checked out your '${card.productName} -> ${card.adType}' product",
                        isRead = false,
                        timestamp = System.currentTimeMillis()
                    )
                    senNotification.sendNotification(card.posterId, requireActivity(), notification)
                }else{
                    Toast.makeText(requireActivity(), "You cannot message yourself", Toast.LENGTH_SHORT).show()
                }
            },
            onPreviewClick = { card ->
                // Open ProductDetail activity
                val intent = Intent(requireActivity(), ProductDetail::class.java).apply {
                    putExtra("adId", card.adId)  // Ensure ad.id is not null
                    putExtra("adType", card.adType)  // Ensure ad.type is not null
                }
                startActivity(intent)
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

      observer()


        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }
    }

    private fun observer(){
        // Observe the ViewModel for updates
        viewModel.popularAdSoap.observe(viewLifecycleOwner) { cards ->
            if (cards.isEmpty()) {
                binding.swipeRefreshLayout.isRefreshing = false
                return@observe
            }

            val shuffledAds = cards.shuffled() // Step 1: Shuffle first
            val combinedList = mutableListOf<Any>()

            shuffledAds.forEachIndexed { index, ad ->
                combinedList.add(ad)
                // Insert an AdPlaceholder every 2nd item
                if ((index + 1) % 2 == 0) {
                    combinedList.add(AdPlaceholder)
                }
            }

            cardAdapter.updateItems(combinedList) // Update adapter with shuffled + modified list

            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun filterAds(country: String, state: String?, city: String?) {
        repository.filterAdsByLocation("Iru Soap", country, state, city) { filteredAds ->
            requireActivity().runOnUiThread {
                if (filteredAds.isEmpty()) {
                    Toast.makeText(requireContext(), "No ads found for the selected filters.", Toast.LENGTH_SHORT).show()
                }
                cardAdapter.updateItems(filteredAds)
            }
        }
    }

    private fun refreshData() {
        observer() // Fetch new data from ViewModel
    }

    override fun onStart() {
        super.onStart()

    }

}
