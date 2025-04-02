package com.cwp.jinja_hub.ui.market_place

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.com.cwp.jinja_hub.listeners.ReselectedListener
import com.cwp.jinja_hub.databinding.FragmentMarketPlaceBinding
import com.cwp.jinja_hub.ui.market_place.buy.Buy
import com.cwp.jinja_hub.ui.market_place.sell.CreateADFragment

class MarketPlaceFragment : Fragment(), ReselectedListener {
   private var _binding: FragmentMarketPlaceBinding? = null
   private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment
        _binding = FragmentMarketPlaceBinding.inflate(inflater, container, false)
        val window = requireActivity().window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Set status bar color
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.black)

        // Make status bar icons light (dark text/icons)
        windowInsetsController.isAppearanceLightStatusBars = false  // Use `false` for light icons
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack() // Go back to the previous fragment
                } else {
                    requireActivity().finish() // Only close if there's nothing to go back to
                }
            }
        })

        binding.buy.setOnClickListener {
            // Open buy fragment
            loadFragment(Buy())
        }

        binding.sell.setOnClickListener {
            // open Create ad fragment
          loadFragment(CreateADFragment())
        }

        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    private fun loadFragment(fragment: Fragment){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.my_market_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {


        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MarketPlaceFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onTabReselected() {
        loadFragment(MarketPlaceFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}