package com.cwp.jinja_hub.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.com.cwp.jinja_hub.adapters.MyADPagerAdapter
import com.cwp.jinja_hub.com.cwp.jinja_hub.listeners.ReselectedListener
import com.cwp.jinja_hub.databinding.FragmentActivityBinding
import com.cwp.jinja_hub.ui.dashboard.ActivityViewModel
import com.cwp.jinja_hub.ui.market_place.buy.MyADViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ActivityFragment : Fragment(), ReselectedListener {

    private var _binding: FragmentActivityBinding? = null
    private val binding get() = _binding!!

    private var lastClickTime: Long = 0

    private lateinit var viewModel: MyADViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activityViewModel = ViewModelProvider(requireActivity())[ActivityViewModel::class.java]
        // Initialize the ViewModel with activity scope to prevent crashes
        viewModel = ViewModelProvider(requireActivity())[MyADViewModel::class.java]

        setupViewPager()
        setupTabLayout()
    }

    private fun setupViewPager() {
        val adapter = MyADPagerAdapter(this)
        binding.viewPager2.adapter = adapter
    }

    private fun setupTabLayout() {
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> "Jinja"
                1 -> "Soap"
                else -> null
            }
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    val tabTextView = it.view?.findViewById<TextView>(android.R.id.text1)
                    tabTextView?.setTextColor(Color.GREEN)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    val tabTextView = it.view?.findViewById<TextView>(android.R.id.text1)
                    tabTextView?.setTextColor(Color.DKGRAY)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // No implementation needed
            }
        })
    }

    private fun loadFragment() {
        val currentTime = System.currentTimeMillis()

        // Prevent rapid double taps (cooldown of 500ms)
        if (currentTime - lastClickTime < 500) return
        lastClickTime = currentTime

        val fragmentManager = requireActivity().supportFragmentManager

        if (view != null && requireActivity().findViewById<View>(R.id.home_container) != null) {
            val existingFragment = fragmentManager.findFragmentByTag(ActivityFragment::class.java.simpleName)

            if (existingFragment == null || !existingFragment.isAdded) {
                fragmentManager.beginTransaction()
                    .replace(R.id.home_container, ActivityFragment(), ActivityFragment::class.java.simpleName)
                    .addToBackStack(null)
                    .commitAllowingStateLoss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevent memory leaks
    }

    override fun onTabReselected() {
        loadFragment()
    }
}
