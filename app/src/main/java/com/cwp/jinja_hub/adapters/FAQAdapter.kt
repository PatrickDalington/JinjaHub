package com.cwp.jinja_hub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.FAQCardItem

class FAQAdapter(
    private var faq: List<FAQCardItem>
) : RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    inner class FAQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.title)
        val content: TextView = itemView.findViewById(R.id.faq_content)
        val toggle: ImageView = itemView.findViewById(R.id.collapse_faq)
        val readMore: TextView = itemView.findViewById(R.id.read_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.faq_item_layout, parent, false)
        return FAQViewHolder(view)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        val faqItem = faq[position]
        holder.title.text = faqItem.title
        holder.content.text = faqItem.content



        // When the toggle is clicked, we toggle the visibility/expansion of the content.
        holder.toggle.setOnClickListener {
            if (holder.content.visibility == View.GONE) {
                // Expand the content: make it visible and limit to 3 lines.
                holder.content.visibility = View.VISIBLE
                holder.content.maxLines = 3
                holder.toggle.setImageResource(R.drawable.minimize_btn)

                // Post a runnable to check the line count after the TextView is laid out.
                holder.content.post {
                    if (holder.content.lineCount <= 3) {
                        // If the content doesn't exceed 3 lines, hide the readMore view.
                        holder.readMore.visibility = View.GONE
                    } else {
                        holder.readMore.visibility = View.VISIBLE
                        // Set the text of readMore to indicate more content is available.
                        holder.readMore.text = "...Read more"
                    }
                }
            } else {
                // Collapse: hide the content and the readMore view.
                holder.content.visibility = View.GONE
                holder.readMore.visibility = View.GONE
                holder.toggle.setImageResource(R.drawable.green_plus)
            }
        }

        // When the readMore view is clicked, toggle between showing more or less.
        holder.readMore.setOnClickListener {
            if (holder.content.visibility == View.VISIBLE && holder.content.maxLines == 3) {
                // Expand to show full content.
                holder.content.maxLines = Int.MAX_VALUE
                holder.readMore.text = "Show less"
            } else if (holder.content.visibility == View.VISIBLE && holder.content.maxLines == Int.MAX_VALUE) {
                // Collapse back to 3 lines.
                holder.content.maxLines = 3
                holder.readMore.text = "...Read more"
            }
        }
    }

    override fun getItemCount(): Int = faq.size




    fun updateFAQList(newFAQList: List<FAQCardItem>) {
        val diffResult = DiffUtil.calculateDiff(FAQDiffCallback(faq, newFAQList))
        faq = newFAQList
        diffResult.dispatchUpdatesTo(this)
    }

    class FAQDiffCallback(
        private val oldList: List<FAQCardItem>,
        private val newList: List<FAQCardItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}
