package com.cwp.jinja_hub.com.cwp.jinja_hub.listeners

import androidx.recyclerview.widget.RecyclerView

interface RefreshableFragmentListener {
    fun onRefreshRequested()
    fun getRecyclerView(): RecyclerView
}