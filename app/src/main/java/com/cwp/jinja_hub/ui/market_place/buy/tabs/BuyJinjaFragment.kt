package com.cwp.jinja_hub.ui.market_place.buy.tabs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.adapters.JinjaDrinkCardAdapter
import com.cwp.jinja_hub.databinding.FragmentBuyJinjaBinding
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.ui.market_place.ADViewModel
import com.cwp.jinja_hub.ui.market_place.buy.BuyViewModel
import com.cwp.jinja_hub.ui.market_place.details.ProductDetail

class BuyJinjaFragment : Fragment() {

    private lateinit var viewModel: ADViewModel
    private lateinit var cardAdapter: JinjaDrinkCardAdapter

    private var _binding: FragmentBuyJinjaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyJinjaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // Initialize the ViewModel
        viewModel = ViewModelProvider(requireActivity())[ADViewModel::class.java]

        // Set up the adapter
         binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        cardAdapter = JinjaDrinkCardAdapter(
            listOf(), // Initialize with an empty list
            onCardClick = { card ->
                // Open ProductDetail activity
                val intent = Intent(requireActivity(), ProductDetail::class.java).apply {
                    putExtra("adId", card.adId)  // Ensure ad.id is not null
                    putExtra("adType", card.adType)  // Ensure ad.type is not null
                }
                startActivity(intent)
            },
            onHeartClick = { card, like ->
                viewModel.likeAD(card.adId.toString(), adType = card.adType, userId = card.posterId, {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                })

            }
        )


//        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
//            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
//        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        binding.recyclerview.adapter = cardAdapter

        // Observe the ViewModel for updates
        viewModel.myAdDrink.observe(viewLifecycleOwner) { cards ->
            cardAdapter.updateCards(cards.distinctBy { it.adId })
        }
    }
}
