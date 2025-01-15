package com.cwp.jinja_hub.model

data class JinjaDrinkCardItem(
    val id: Int = 0,
    val title: String = "",
    val oldPrice: String = "",
    val newPrice: String = "",
    val imageResId: Int = 0,
    var isLiked: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JinjaDrinkCardItem) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}