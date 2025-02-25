package com.cwp.jinja_hub.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.model.LatestCommentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class LatestCommentsAdapter(
    var comments: List<LatestCommentModel>,
    private val onCommentClicked: (LatestCommentModel, position:Int) -> Unit,
) : RecyclerView.Adapter<LatestCommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.comment_text)
        val timestamp: TextView = view.findViewById(R.id.comment_timestamp)
        val profileImage: CircleImageView = view.findViewById(R.id.profile_image)
        val name: TextView = view.findViewById(R.id.poster_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.latest_comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val pos = position
        val comment = comments[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Set comment details
        holder.text.text = comment.text
        holder.name.text = comment.name
        holder.timestamp.text = SimpleDateFormat("hh:mm", Locale.getDefault()).format(
            Date(comment.timestamp)
        )
        holder.profileImage.load(comment.imageUrl)

        // Handle long-press gesture
        holder.itemView.setOnLongClickListener {
            onCommentClicked(comment, position)
           true
        }
    }

    override fun getItemCount(): Int = comments.size




    fun updateComments(newComments: List<LatestCommentModel>, pos: Int) {
        this.comments = newComments
        notifyItemRemoved(pos)  // Refresh RecyclerView UI
    }
}
