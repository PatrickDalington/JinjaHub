package com.cwp.jinja_hub.ui.jinja_product

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.JinjaProductPagerAdapter
import com.cwp.jinja_hub.databinding.FragmentJinjaProductBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JinjaProduct : Fragment() {

    private var _binding: FragmentJinjaProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentJinjaProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            try {
                MobileAds.initialize(requireActivity()) { status ->
                    Log.d("AdMob", "AdMob Initialized: $status")
                }
            } catch (e: Exception) {
                Log.e("AdMobError", "Failed to initialize AdMob: ${e.message}")
            }
        }

        // Find AdView as defined in the layout
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)


        // Define layouts and titles for ViewPager2
        val layouts = listOf(
            R.layout.jinja_product_description,
            R.layout.jinja_product_benefits,
            R.layout.jinja_product_usage
        )
        val titles = listOf("Description", "Benefits", "Usage")

        // Set up ViewPager2 adapter
        val adapter = JinjaProductPagerAdapter(layouts, titles)
        binding.viewPager2.adapter = adapter

        // Attach TabLayoutMediator with custom tabs
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            val customTabView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_tab, null)
            val textView = customTabView.findViewById<TextView>(R.id.title)
            textView.text = titles[position]
            tab.customView = customTabView
        }.attach()


        // Handle back button press to navigate back to HomeFragment
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Navigate back to HomeFragment
                    findNavController().navigate(R.id.navigation_home)
                }
            }
        )

        // Customize TabLayout tab colors on selection and unselection
        setupTabColors()
    }

    private fun setupTabColors() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.title)?.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // Selected text color
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.findViewById<TextView>(R.id.title)?.apply {
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.grey)) // Unselected text color
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Optional: Define behavior for reselection if needed
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
