package com.cwp.jinja_hub.adapters

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.LatestCommentModel
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class LatestCommentsAdapter(
    private var newsId:String,
    var comments: List<LatestCommentModel>,
    private val onCommentClicked: (LatestCommentModel, position:Int) -> Unit,
    private val onDeleteButtonClicked: (LatestCommentModel, position:Int) -> Unit
) : RecyclerView.Adapter<LatestCommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.comment_text)
        val timestamp: TextView = view.findViewById(R.id.comment_timestamp)
        val profileImage: CircleImageView = view.findViewById(R.id.profile_image)
        val name: TextView = view.findViewById(R.id.poster_name)
        val deleteCommentByPostOwner: ImageView
        = view.findViewById(R.id.delete_comment_by_post_owner)
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

         if ( newsId== currentUserId) {
             holder.deleteCommentByPostOwner.visibility = View.VISIBLE
             holder.deleteCommentByPostOwner.setOnClickListener {
                 onDeleteButtonClicked(comment, pos)
             }
         } else {

             holder.deleteCommentByPostOwner.visibility = View.GONE
         }
        // Set comment details
        holder.text.text = comment.text

        if (comment.name == "null") {
            holder.name.text = "Deleted User"
            holder.name.setTypeface(Typeface.DEFAULT, Typeface.ITALIC)
        }
        else {
            holder.name.text = comment.name
            holder.profileImage.load(comment.imageUrl){
                placeholder(R.drawable.no_img)
            }
        }
        holder.timestamp.text = SimpleDateFormat("hh:mm", Locale.getDefault()).format(
            Date(comment.timestamp)
        )


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
