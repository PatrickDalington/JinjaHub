package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class BuyPagerAdapter(private val layouts: List<Int>, private val title: List<String>) :
    RecyclerView.Adapter<BuyPagerAdapter.SliderViewHolder>() {

    inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        // No binding necessary if layouts are static
    }

    override fun getItemCount(): Int = layouts.size

    fun getPageTitle(position: Int) : String = title[position]

    override fun getItemViewType(position: Int): Int {
        // Return the layout resource ID for the given position
        return layouts[position]
    }
}