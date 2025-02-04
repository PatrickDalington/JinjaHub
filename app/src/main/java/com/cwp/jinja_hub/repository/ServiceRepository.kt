package com.cwp.jinja_hub.repository

import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.CardItem
import com.cwp.jinja_hub.model.ServicesCategory
import kotlinx.coroutines.delay

class ServiceRepository {

    // Simulate fetching categories
    suspend fun fetchCategories(): List<ServicesCategory> {
        delay(1000) // Simulate network delay
        return listOf(
            ServicesCategory(1, "Therapist", true),
            ServicesCategory(2, "Surgeon", false),
            ServicesCategory(3, "Pediatrician", false),
            ServicesCategory(4, "Neurologist", false)
        )
    }

    // Simulate fetching cards for a specific category
    suspend fun fetchCardsForCategory(category: String): List<CardItem> {
        delay(1000) // Simulate network delay
        return when (category) {
            "Therapist" -> listOf(
                CardItem(1, "Cognitive Therapy", "Therapist", R.drawable.profile_image),
                CardItem(2, "Behavioral Therapy", "Therapist", R.drawable.profile_image)
            )
            "Surgeon" -> listOf(
                CardItem(3, "Cardiac Surgery", "Surgeon", R.drawable.profile_image),
                CardItem(4, "Neurosurgery", "Surgeon", R.drawable.profile_image)
            )
            "Pediatrician" -> listOf(
                CardItem(5, "Child Health Care", "Pediatrician", R.drawable.profile_image),
                CardItem(6, "Immunization", "Pediatrician", R.drawable.profile_image)
            )
            "Neurologist" -> listOf(
                CardItem(7, "Brain Disorders", "Neurologist", R.drawable.profile_image),
                CardItem(8, "Spinal Cord Injuries", "Neurologist", R.drawable.profile_image)
            )
            else -> emptyList()
        }
    }
}
