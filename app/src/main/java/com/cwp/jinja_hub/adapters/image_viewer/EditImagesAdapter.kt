package com.cwp.jinja_hub.adapters.image_viewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import com.otaliastudios.zoom.ZoomImageView

class EditImagesAdapter(
    private val imageUrls: MutableList<String>,
    private val onDeleteClickListener: (String, Int) -> Unit
) :
    RecyclerView.Adapter<EditImagesAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.edit_review_photos_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        holder.reviewImage.load(imageUrl)

        holder.deleteImage.setOnClickListener {
            onDeleteClickListener(imageUrl, position)
        }
    }

    override fun getItemCount(): Int = imageUrls.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reviewImage: ImageView = itemView.findViewById(R.id.review_image)
        val deleteImage: ImageView = itemView.findViewById(R.id.delete)
    }
}