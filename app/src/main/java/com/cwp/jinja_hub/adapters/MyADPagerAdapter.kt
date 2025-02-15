package com.cwp.jinja_hub.com.cwp.jinja_hub.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.activity.buy.tabs.MyJinjaFragment
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.activity.buy.tabs.MyJinjaSoapFragment

class MyADPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MyJinjaFragment()
            1 -> MyJinjaSoapFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")

        }
    }
}