package com.cwp.jinja_hub.utils

class NumberFormater {
    fun formatNumber(number: String): String {
        return try {
            val num = number.toDouble()
            when {
                num >= 1_000_000 -> String.format("%.1fm", num / 1_000_000)
                num >= 1_000 -> String.format("%.1fk", num / 1_000)
                else -> number // Return the original number if it's less than 1,000
            }
        } catch (e: NumberFormatException) {
            "Invalid number"
        }
    }
}