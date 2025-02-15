package com.cwp.jinja_hub.adapters

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.NotificationModel
import java.text.DateFormat

class NotificationAdapter(
    private val onNotificationClicked: (NotificationModel, Int) -> Unit
) : ListAdapter<NotificationModel, NotificationAdapter.NotificationViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NotificationModel>() {
            override fun areItemsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
                return oldItem == newItem
            }
        }
    }

    // Handler and Runnable for periodic refresh.
    private var handler: Handler? = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = object : Runnable {
        override fun run() {
            // This will rebind all items and recalculate the display time.
            notifyDataSetChanged()
            handler?.postDelayed(this, 60000) // update every 60 seconds
        }
    }

    init {
        handler?.post(runnable!!)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        handler?.removeCallbacks(runnable!!)
        handler = null
        runnable = null
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentTextView: TextView = itemView.findViewById(R.id.content)
        private val dateTextView: TextView = itemView.findViewById(R.id.date)

        fun bind(item: NotificationModel) {
            contentTextView.text = item.content

            val now = System.currentTimeMillis()
            val timeDifference = now - item.timestamp


            val date = DateFormat.getTimeInstance(DateFormat.SHORT).format(item.timestamp)

            dateTextView.text = date

            // Update the typeface based on read status.
            contentTextView.typeface = if (item.isRead) {
                Typeface.DEFAULT
            } else {
                Typeface.DEFAULT_BOLD
            }

            itemView.setOnClickListener {
                onNotificationClicked(item, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
