package com.cwp.jinja_hub.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
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
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.google.firebase.auth.FirebaseAuth

class JinjaDrinkCardAdapter(
    private var cards: List<ADModel>,
    private val onMessageClick: (ADModel) -> Unit,
    private val onPreviewClick: (ADModel) -> Unit,
    private val repository: ADRepository
) : RecyclerView.Adapter<JinjaDrinkCardAdapter.ServiceCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ad_item, parent, false)
        return ServiceCategoryViewHolder(view)
    }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: ServiceCategoryViewHolder, position: Int) {
        val card = cards[position]

        // Set alternating background colors
//        val backgroundColor = if (position % 2 == 0) {
//            holder.itemView.context.getColor(R.color.light_yellow) // Use white for even positions
//        } else {
//            holder.itemView.context.getColor(R.color.light_green) // Use black for odd positions
//        }
//        holder.card.setBackgroundColor(backgroundColor)



        holder.cardTitle.text = card.productName
        holder.cardImage.load(card.mediaUrl?.get(0))
        holder.newPrice.text = card.amount
        holder.city.text = card.city
        holder.state.text = card.state + ", "
        holder.country.text = card.country
        holder.description.text = card.description



        // Get profile image from card posterId
        ADViewModel(repository).fetchUserDetails(card.posterId) { fullName, username, profileImage, _ -> run {

                holder.profileImage.load(profileImage)
            }
        }


        holder.message.setOnClickListener{
            // Go to message activity
           onMessageClick(card)
        }

        holder.preview.setOnClickListener{
            onPreviewClick(card)
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
        val newPrice: TextView = itemView.findViewById(R.id.new_price)
        val card: RelativeLayout = itemView.findViewById(R.id.card)
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val city: TextView = itemView.findViewById(R.id.city)
        val state: TextView = itemView.findViewById(R.id.state)
        val country: TextView = itemView.findViewById(R.id.country)
        val description: TextView = itemView.findViewById(R.id.description)
        val message: TextView = itemView.findViewById(R.id.message)
        val preview: TextView = itemView.findViewById(R.id.preview)
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
