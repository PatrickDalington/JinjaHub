package com.cwp.jinja_hub.ui.testimony_reviews

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.ReviewPagerAdapter
import com.cwp.jinja_hub.databinding.FragmentReviewsBinding
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.LatestFragment
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.MyReviewFragment
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.Popular
import com.cwp.jinja_hub.utils.NavigateTo
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class Reviews : Fragment() {

    // declear binding
    private var _binding: FragmentReviewsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to AddReviewFragment
        binding.addReview.setOnClickListener {
            val addReviewFragment = AddReview()
            parentFragmentManager.beginTransaction()
                .replace(R.id.review_container, addReviewFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.backButton.setOnClickListener {
            // Handle back button press to navigate back to HomeFragment
            NavigateTo().navigateToHome(findNavController())
        }

        // Initialize the adapter
        val adapter = ReviewPagerAdapter(this) // Pass `this` fragment
        adapter.addFragment(Popular(), "Popular")
        adapter.addFragment(LatestFragment(), "Latest")
        adapter.addFragment(MyReviewFragment(), "My Review")
        binding.viewPager2.adapter = adapter

        // Attach the TabLayoutMediator
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            // Inflate custom tab layout
            val tabView = LayoutInflater.from(context).inflate(R.layout.review_tab_layout_text, null) as TextView
            tabView.text = adapter.getPageTitle(position)
            tab.customView = tabView
        }.attach()

        // Change tab text color on selection
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.customView?.let {
                    val tabTextView = it.findViewById<TextView>(R.id.tabTextView)
                    tabTextView.setTextColor(resources.getColor(R.color.primary))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.customView?.let {
                    val tabTextView = it.findViewById<TextView>(R.id.tabTextView)
                    tabTextView.setTextColor(Color.BLACK)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // No action needed
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Navigate back to HomeFragment
                    NavigateTo().navigateToHome(findNavController())
                }
            }
        )



    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Reviews().apply {
                arguments = Bundle().apply { }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}