package com.cwp.jinja_hub.ui.market_place.buy

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.adapters.BuyJinjaPagerAdapter
import com.cwp.jinja_hub.databinding.FragmentBuyBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class Buy : Fragment() {
    private var _binding: FragmentBuyBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BuyViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the ViewModel
        viewModel = ViewModelProvider(this)[BuyViewModel::class.java]

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
    }
}
