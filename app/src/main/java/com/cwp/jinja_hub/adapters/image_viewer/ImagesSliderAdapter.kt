package com.cwp.jinja_hub.adapters.image_viewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import com.otaliastudios.zoom.ZoomImageView

class ImagesSliderAdapter(private val imageUrls: List<String>) :
    RecyclerView.Adapter<ImagesSliderAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        holder.zoomImageView.load(imageUrl){
            crossfade(true)
            crossfade(500)
            placeholder(R.drawable.loading)
        }
    }

    override fun getItemCount(): Int = imageUrls.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val zoomImageView: ZoomImageView = itemView.findViewById(R.id.zoomImageView)
    }
}