package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.CardItem
import com.cwp.jinja_hub.model.ServicesCategory
import androidx.recyclerview.widget.DiffUtil

class ServiceCardAdapter(
    private var cards: List<CardItem>,
    private val selectedCategory: ServicesCategory,
    private val onCardClick: (CardItem, ServicesCategory) -> Unit,
    private val onFirstCardClick: (CardItem) -> Unit
) : RecyclerView.Adapter<ServiceCardAdapter.ServiceCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.service_category_card_item, parent, false)
        return ServiceCategoryViewHolder(view)
    }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: ServiceCategoryViewHolder, position: Int) {
        val card = cards[position]
        holder.cardTitle.text = card.title
        holder.cardImage.setImageResource(card.imageResId)

        holder.itemView.setOnClickListener {
            onCardClick(card, selectedCategory)
        }

        // Handle the first card click specifically
        if (position == 0) {
            holder.itemView.setOnClickListener {
                Toast.makeText(holder.itemView.context, "category: ${card.title}", Toast.LENGTH_SHORT).show()
                onFirstCardClick(card)
            }
        }else{
            holder.itemView.setOnClickListener {
                Toast.makeText(holder.itemView.context, "${card.title} coming soon", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to update cards with DiffUtil
    fun updateCards(newCards: List<CardItem>) {
        val diffCallback = CardDiffCallback(cards, newCards)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        cards = newCards
        diffResult.dispatchUpdatesTo(this)
    }

    class ServiceCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardTitle: TextView = itemView.findViewById(R.id.card_title)
        val cardImage: ImageView = itemView.findViewById(R.id.card_image)
    }

    // DiffUtil Callback for ServiceCardAdapter
    class CardDiffCallback(
        private val oldList: List<CardItem>,
        private val newList: List<CardItem>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id  // Assuming 'id' is unique
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
