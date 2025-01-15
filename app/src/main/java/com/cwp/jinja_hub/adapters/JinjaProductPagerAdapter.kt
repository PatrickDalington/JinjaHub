package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class JinjaProductPagerAdapter(
    private val layouts: List<Int>,
    private val titles: List<String> // If not required, this can be removed
) : RecyclerView.Adapter<JinjaProductPagerAdapter.SliderViewHolder>() {

    // ViewHolder to hold the inflated views
    inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        // Inflate the layout corresponding to the view type
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return SliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {
        // No binding logic necessary for static layouts
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int {
        // Return the layout resource ID for this position
        return layouts[position]
    }
}
