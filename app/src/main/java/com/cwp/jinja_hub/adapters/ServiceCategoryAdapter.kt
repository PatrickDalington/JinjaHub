package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.ServicesCategory

class ServiceCategoryAdapter(
    private var categories: List<ServicesCategory>,
    private val onCategorySelected: (ServicesCategory) -> Unit
) : RecyclerView.Adapter<ServiceCategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = -1

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryName: TextView = itemView.findViewById(R.id.category_name)

        fun bind(category: ServicesCategory, position: Int) {
            categoryName.text = category.name
            itemView.isSelected = position == selectedPosition

            // Highlight selected item
            itemView.setBackgroundResource(
                if (position == selectedPosition) android.R.color.holo_blue_light
                else android.R.color.transparent
            )

            // Handle item click
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onCategorySelected(category)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.service_category_item, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position)
    }

    fun updateCategories(newCategories: List<ServicesCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = categories.size
}
