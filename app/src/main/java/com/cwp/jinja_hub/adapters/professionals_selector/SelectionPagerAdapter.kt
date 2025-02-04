package com.cwp.jinja_hub.adapters.professionals_selector

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SelectionPagerAdapter(
    activity: FragmentActivity,
    private val fragmentList: List<Fragment>
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = fragmentList.size
    override fun createFragment(position: Int): Fragment = fragmentList[position]
}