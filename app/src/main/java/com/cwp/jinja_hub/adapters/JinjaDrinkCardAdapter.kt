package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.CardItem
import com.cwp.jinja_hub.model.ServicesCategory
import androidx.recyclerview.widget.DiffUtil
import com.cwp.jinja_hub.model.JinjaDrinkCardItem
import com.cwp.jinja_hub.repository.JinjaMarketRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JinjaDrinkCardAdapter(
    private var cards: List<JinjaDrinkCardItem>,
    private val onCardClick: (JinjaDrinkCardItem) -> Unit,
    private val onHeartClick: (JinjaDrinkCardItem, Boolean) -> Unit
) : RecyclerView.Adapter<JinjaDrinkCardAdapter.ServiceCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceCategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.jinja_drink_recycler_item, parent, false)
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
        holder.card.setBackgroundColor(backgroundColor)



        holder.cardTitle.text = card.title
        holder.cardImage.setImageResource(card.imageResId)
        holder.oldPrice.text = card.oldPrice
        holder.newPrice.text = card.newPrice

        holder.itemView.setOnClickListener {
            onCardClick(card)
        }

        // Set heart icon based on "liked" status
        val userLikedRef = FirebaseDatabase.getInstance().getReference("user_likes")
            .child(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .child(card.id.toString())

        userLikedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.value == true) {
                    holder.heart.setImageResource(R.drawable.spec_heart_on)
                } else {
                    holder.heart.setImageResource(R.drawable.heart)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })




        holder.heart.setOnClickListener {
            val currentLiked = holder.heart.drawable.constantState ==
                    holder.itemView.context.getDrawable(R.drawable.spec_heart_on)?.constantState

            val newLiked = !currentLiked
            holder.heart.setImageResource(
                if (newLiked) R.drawable.spec_heart_on else R.drawable.heart
            )

            // Update the database
            JinjaMarketRepository().likeJinjaDrink(
                card.id.toString(),
                newLiked,
                onSuccess = { status ->
                    Toast.makeText(holder.itemView.context, "Card $status", Toast.LENGTH_SHORT).show()
                },
                onFailure = {
                    Toast.makeText(holder.itemView.context, "Failed to update like", Toast.LENGTH_SHORT).show()
                }
            )
        }



        holder.cart.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Cart Clicked", Toast.LENGTH_SHORT).show()
        }

    }

    // Method to update cards with DiffUtil
    fun updateCards(newCards: List<JinjaDrinkCardItem>) {
        val diffCallback = CardDiffCallback(cards, newCards)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        cards = newCards
        diffResult.dispatchUpdatesTo(this)
    }

    class ServiceCategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val cardTitle: TextView = itemView.findViewById(R.id.title)
        val cardImage: ImageView = itemView.findViewById(R.id.image)
        val heart: ImageView = itemView.findViewById(R.id.heart)
        val cart: ImageView = itemView.findViewById(R.id.cart)
        val oldPrice: TextView = itemView.findViewById(R.id.old_price)
        val newPrice: TextView = itemView.findViewById(R.id.new_price)
        val card: CardView = itemView.findViewById(R.id.card)
    }

    // DiffUtil Callback for ServiceCardAdapter
    class CardDiffCallback(
        private val oldList: List<JinjaDrinkCardItem>,
        private val newList: List<JinjaDrinkCardItem>
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
