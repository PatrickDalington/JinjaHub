package com.cwp.jinja_hub.tabs

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
import com.cwp.jinja_hub.ui.market_place.buy.BuyViewModel

class BuyJinjaFragment : Fragment() {

    private lateinit var viewModel: BuyViewModel
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
        viewModel = ViewModelProvider(requireActivity())[BuyViewModel::class.java]

        // Set up the adapter
         binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
        cardAdapter = JinjaDrinkCardAdapter(
            listOf(), // Initialize with an empty list
            onCardClick = { card ->
                Toast.makeText(requireActivity(), "Card clicked: ${card.title}", Toast.LENGTH_SHORT).show()
            },
            onHeartClick = { card, like ->
                viewModel.updateLikeStatus(card.id.toString(), like)
            }
        )


        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        binding.recyclerview.adapter = cardAdapter

        // Observe the ViewModel for updates
        viewModel.cards.observe(viewLifecycleOwner) { cards ->
            cardAdapter.updateCards(cards.distinctBy { it.id })
        }
    }
}
