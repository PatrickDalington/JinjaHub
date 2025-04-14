package com.cwp.jinja_hub.ui.market_place.buy

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.BuyJinjaPagerAdapter
import com.cwp.jinja_hub.databinding.FragmentBuyBinding
import com.cwp.jinja_hub.ui.market_place.buy.tabs.BuyJinjaFragment
import com.cwp.jinja_hub.ui.market_place.buy.tabs.BuyJinjaSoapFragment
import com.cwp.jinja_hub.ui.market_place.filter.FilterFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class Buy : Fragment() {
    private var _binding: FragmentBuyBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MyADViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyBinding.inflate(inflater, container, false)
        val window = requireActivity().window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Set status bar color
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        // Make status bar icons light (dark text/icons)
        windowInsetsController.isAppearanceLightStatusBars = true  // Use `false` for light icons
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this)[MyADViewModel::class.java]

        // Set up the ViewPager2 with the FragmentStateAdapter
        val adapter = BuyJinjaPagerAdapter(this)


        binding.viewPager2.adapter = adapter

        // Set up TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> "Jinja"
                1 -> "Soap"
                else -> null
            }
        }.attach()



        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.viewPager2.currentItem > 0) {
                    // Move to previous tab instead of exiting
                    binding.viewPager2.currentItem -= 1
                } else {
                    // Exit `Buy` fragment and return to `MarketPlaceFragment`
                    parentFragmentManager.popBackStack()
                }
            }
        })


        // Change tab text color on selection
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val tabTextView = it.view.findViewById<TextView>(android.R.id.text1)
                    tabTextView?.setTextColor(Color.GREEN)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    val tabTextView = it.view.findViewById<TextView>(android.R.id.text1)
                    tabTextView?.setTextColor(Color.DKGRAY)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // No implementation needed
            }
        })

        binding.sort.setOnClickListener{
            // Open sort fragment

            loadFragment(FilterFragment())
        }




    }

    private fun passFilterDataToFragment(fragment: Fragment, country: String, state: String, area: String) {
        val bundle = Bundle().apply {
            putString("selected_country", country)
            putString("selected_state", state)
            putString("selected_area", area)
        }

        fragment.arguments = bundle

        childFragmentManager.beginTransaction()
            .replace(R.id.buy_container, fragment) // Ensure `buy_container` exists in your layout
            .commit()
    }

    private fun getCurrentTab(): String {
        return binding.tabLayout.getTabAt(binding.tabLayout.selectedTabPosition)?.text.toString()
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = childFragmentManager.beginTransaction() // Use childFragmentManager
        transaction.replace(R.id.buy_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
