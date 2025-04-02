package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.market_place.ADViewModel

class MyADDrinkCardAdapter(
    var cards: List<ADModel>,
    private val onCardDeleteClick: (ADModel, Int) -> Unit,
    private val onCardEditClick: (ADModel, Int) -> Unit,
    private val viewModel: ADViewModel // Pass ViewModel instead of creating it inside onBindViewHolder
) : RecyclerView.Adapter<MyADDrinkCardAdapter.ServiceCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_ad_item, parent, false)
        return ServiceCategoryViewHolder(view)
    }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: ServiceCategoryViewHolder, position: Int) {
        val card = cards[position]

        // Set alternating background colors
        val backgroundColor = if (position % 2 == 0) {
            holder.itemView.context.getColor(R.color.light_yellow) // Light yellow for even positions
        } else {
            holder.itemView.context.getColor(R.color.light_green) // Light green for odd positions
        }
        holder.itemView.setBackgroundColor(backgroundColor) // Apply the background color

        // Set card details
        holder.cardTitle.text = card.productName
        holder.cardImage.load(card.mediaUrl?.getOrNull(0)) // Avoid index out of bounds
        holder.newPrice.text = if (card.currency == "Dollar ($)") {
            "$${card.amount}"
        } else {
            "₦${card.amount}"
        }
        holder.description.text = card.description

        // Fetch profile details using the ViewModel
        viewModel.fetchUserDetails(card.posterId) { fullName, username, profileImage, _ ->
            // Uncomment if using profile images
            // holder.profileImage.load(profileImage)
        }

        holder.deleteButton.setOnClickListener {
            onCardDeleteClick(card, position)
        }

        holder.editButton.setOnClickListener {
            onCardEditClick(card, position)
        }
    }

    // Method to update cards with DiffUtil
    fun updateCards(newCards: List<ADModel>) {
        val diffCallback = CardDiffCallback(cards, newCards)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        cards = newCards
        diffResult.dispatchUpdatesTo(this)
    }

    class ServiceCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardTitle: TextView = itemView.findViewById(R.id.title)
        val cardImage: ImageView = itemView.findViewById(R.id.image)
        val description: TextView = itemView.findViewById(R.id.description)
        val newPrice: TextView = itemView.findViewById(R.id.new_price)
        val deleteButton: TextView = itemView.findViewById(R.id.delete)
        val editButton: TextView = itemView.findViewById(R.id.edit)
        // val card: CardView = itemView.findViewById(R.id.card)
        // val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
    }

    // DiffUtil Callback for efficient updates
    class CardDiffCallback(
        private val oldList: List<ADModel>,
        private val newList: List<ADModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].adId == newList[newItemPosition].adId  // Unique ID comparison
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]  // Check object equality
        }
    }
}
