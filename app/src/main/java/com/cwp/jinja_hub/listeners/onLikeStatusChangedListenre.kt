package com.cwp.jinja_hub.com.cwp.jinja_hub.listeners

interface OnLikeStatusChangedListener {
    fun onLikeStatusChanged(reviewId: String, isLiked: Boolean, posterId: String)
}