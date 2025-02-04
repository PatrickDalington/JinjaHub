package com.cwp.jinja_hub.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cwp.jinja_hub.ui.market_place.buy.tabs.BuyJinjaFragment
import com.cwp.jinja_hub.ui.market_place.buy.tabs.BuyJinjaSoapFragment

class BuyJinjaPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BuyJinjaFragment()
            1 -> BuyJinjaSoapFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")

        }
    }
}