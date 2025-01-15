package com.cwp.jinja_hub.ui.specialist_profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.SpecialistPagerAdapter
import com.cwp.jinja_hub.databinding.FragmentSpecialistProfileBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class SpecialistProfileFragment : Fragment() {

    private var _binding: FragmentSpecialistProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSpecialistProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // List of layouts for ViewPager2
        val layouts = listOf(
            R.layout.specialist_info_item_slider,
            R.layout.specialist_experience_item_slider,
            R.layout.specialist_rating_item_slider,
        )

        val titles = listOf("Info", "Experiences", "Reviews")

        val adapter = SpecialistPagerAdapter(layouts, titles)
        binding.viewPager2.adapter = adapter

        // Attach TabLayoutMediator to synchronize TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        // Set the first tab as selected
        binding.tabLayout.getTabAt(1)?.select()


        // Add TabSelectedListener to customize tab background and text colors
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.view?.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.tab_selected_background)

                tab?.view?.let { tabView ->
                    (tabView as ViewGroup).forEach { child ->
                        if (child is TextView) {
                            child.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.white)
                            )
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.view?.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.tab_default_background)

                tab?.view?.let { tabView ->
                    (tabView as ViewGroup).forEach { child ->
                        if (child is TextView) {
                            child.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.black)
                            )
                        }
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Optional: Define behavior for reselected tabs
            }
        })
    }

    override fun onStart() {
        super.onStart()
        // Set the first tab as selected
        binding.tabLayout.getTabAt(0)?.select()
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SpecialistProfileFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}