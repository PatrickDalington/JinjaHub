package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R

class OnboardingPagerAdapter(private val layouts: List<Int>,  private val onChildClick: (view: View, position: Int) -> Unit) :
    RecyclerView.Adapter<OnboardingPagerAdapter.SliderViewHolder>() {

    // ViewHolder for each page
    inner class SliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        // Inflate the layout for the current viewType (layout resource ID)
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return SliderViewHolder(view)
    }



    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {

        val itemView = holder.itemView

        // Access child views for the last layout (client registration)
        val clientCard = itemView.findViewById<View>(R.id.client_card)
        clientCard?.setOnClickListener {
            onChildClick(it, position)
        }

        // Access child views for the last layout (specialist registration)
        val specialistCard = itemView.findViewById<View>(R.id.specialist_card)
        specialistCard?.setOnClickListener {
            onChildClick(it, position)
        }
    }

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int {
        // Return the layout resource ID for the given position
        return layouts[position]
    }
}
