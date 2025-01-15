package com.cwp.jinja_hub.model

data class ServicesCategory(
    val id: Int,                     // Unique identifier for the category
    val name: String,                // Name of the category
    var isSelected: Boolean = false, // Indicates if the category is currently selected
    val description: String? = null  // Optional description for the category
)
