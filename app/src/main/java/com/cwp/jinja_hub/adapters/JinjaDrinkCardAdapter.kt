package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.market_place.ADViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

// A simple marker object for ad placeholders


class JinjaDrinkCardAdapter(
    private var items: List<Any>,  // Now accepts both ADModel and AdPlaceholder
    private val onMessageClick: (ADModel) -> Unit,
    private val onPreviewClick: (ADModel) -> Unit,
    private val repository: ADRepository
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CONTENT = 0
        private const val VIEW_TYPE_AD = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ADModel -> VIEW_TYPE_CONTENT
            is AdPlaceholder -> VIEW_TYPE_AD
            else -> VIEW_TYPE_CONTENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_AD) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.ad_mob_drink_item, parent, false)
            AdViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.ad_item, parent, false)
            ContentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AdViewHolder) {
            // Load the AdMob ad
            val adRequest = AdRequest.Builder().build()
            holder.adView.loadAd(adRequest)
        } else if (holder is ContentViewHolder) {
            val card = items[position] as ADModel

            holder.cardTitle.text = card.productName
            holder.cardImage.load(card.mediaUrl?.get(0))
            holder.newPrice.text = card.amount
            holder.city.text = card.city
            holder.state.text = "${card.state}, "
            holder.country.text = card.country
            holder.description.text = card.description

            // Fetch and load profile image using your ADViewModel
            ADViewModel(repository).fetchUserDetails(card.posterId) { fullName, username, profileImage, _ ->
                holder.profileImage.load(profileImage)
            }

            holder.message.setOnClickListener {
                onMessageClick(card)
            }
            holder.preview.setOnClickListener {
                onPreviewClick(card)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // If you want to update items using DiffUtil
    fun updateItems(newItems: List<Any>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = items[oldItemPosition]
                val newItem = newItems[newItemPosition]
                return if (oldItem is ADModel && newItem is ADModel) {
                    oldItem.adId == newItem.adId
                } else oldItem is AdPlaceholder && newItem is AdPlaceholder
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = items[oldItemPosition]
                val newItem = newItems[newItemPosition]
                return if (oldItem is ADModel && newItem is ADModel) {
                    oldItem == newItem
                } else true
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    // ViewHolder for content items
    inner class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    // ViewHolder for Ad items
    inner class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val adView: AdView = itemView.findViewById(R.id.adView)
    }
}
