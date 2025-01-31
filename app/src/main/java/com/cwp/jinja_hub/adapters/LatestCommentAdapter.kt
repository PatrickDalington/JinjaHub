package com.cwp.jinja_hub.adapters

import com.cwp.jinja_hub.model.LatestCommentModel


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.cwp.jinja_hub.R
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class LatestCommentsAdapter(private val comments: List<LatestCommentModel>) : RecyclerView.Adapter<LatestCommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.comment_text)
        val timestamp: TextView = view.findViewById(R.id.comment_timestamp)
        val profileImage : CircleImageView = view.findViewById(R.id.profile_image)
        val name : TextView = view.findViewById(R.id.poster_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.latest_comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.text.text = comment.text
        holder.name.text = comment.name
        holder.timestamp.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(comment.timestamp))
        holder.profileImage.load(comment.imageUrl)
    }

    override fun getItemCount(): Int = comments.size
}
