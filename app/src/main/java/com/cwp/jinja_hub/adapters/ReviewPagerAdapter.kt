package com.cwp.jinja_hub.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.BlankFragment
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.LatestFragment
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.MyReviewFragment
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.Popular

class ReviewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val fragmentList: MutableList<Fragment> = ArrayList()
    private val titleList: MutableList<String> = ArrayList()

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        titleList.add(title)
    }

    fun getPageTitle(position: Int): CharSequence? {
        return titleList[position]
    }
}
