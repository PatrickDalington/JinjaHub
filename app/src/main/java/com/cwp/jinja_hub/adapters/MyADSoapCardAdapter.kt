package com.cwp.jinja_hub.com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.model.JinjaDrinkCardItem
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.market_place.ADViewModel

class MyADSoapCardAdapter(
    var cards: List<ADModel>,
    private val onCardDeleteClick: (ADModel, Int) -> Unit,
    private val onCardEditClick: (ADModel, Int) -> Unit,
    private val adRepository: ADRepository
) : RecyclerView.Adapter<MyADSoapCardAdapter.ServiceCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_ad_soap_item, parent, false)
        return ServiceCategoryViewHolder(view)
    }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: ServiceCategoryViewHolder, position: Int) {
        val card = cards[position]

        // Set alternating background colors
        val backgroundColor = if (position % 2 == 0) {
            holder.itemView.context.getColor(R.color.light_yellow) // Use white for even positions
        } else {
            holder.itemView.context.getColor(R.color.light_green) // Use black for odd positions
        }
        //holder.card.setBackgroundColor(backgroundColor)


        holder.cardTitle.text = card.productName
        holder.cardImage.load(card.mediaUrl?.get(0))
        if (card.currency == "Dollar ($)")
            holder.newPrice.text = "$${card.amount}"
        else
            holder.newPrice.text = "₦${card.amount}"
        holder.description.text = card.description


        // Get profile image from card posterId
        ADViewModel(adRepository).fetchUserDetails(card.posterId) { fullName, username, profileImage, _ -> run {

            // holder.profileImage.load(profileImage)
        }
        }



        holder.itemView.setOnClickListener {
            //onCardClick(card)
        }

        holder.deleteButton.setOnClickListener{
            onCardDeleteClick(card, position)
        }

        holder.editButton.setOnClickListener{
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
        //val card: CardView = itemView.findViewById(R.id.card)
        //val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
    }

    // DiffUtil Callback for ServiceCardAdapter
    class CardDiffCallback(
        private val oldList: List<ADModel>,
        private val newList: List<ADModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].adId == newList[newItemPosition].adId  // Assuming 'id' is unique
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
