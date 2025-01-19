package com.cwp.jinja_hub.repository

import ConsultationModel
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.CardItem
import com.cwp.jinja_hub.model.ServicesCategory
import kotlinx.coroutines.delay

class ServiceRepository {

    // Simulate fetching categories
    suspend fun fetchCategories(): List<ServicesCategory> {
        delay(1000) // Simulate network delay
        return listOf(
            ServicesCategory(1, "Therapist", false),
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
                CardItem(1,"Cognitive Therapy", "Therapist", R.drawable.profile_image),
                CardItem(2, "Behavioral Therapy","Therapist", R.drawable.profile_image)
            )

            else -> emptyList()
        }
    }

    // Simulate fetching specialists for a specific category
    suspend fun fetchSpecialistsForCategory(category: String): List<ConsultationModel> {
        delay(1000) // Simulate network delay
        return when (category) {
            "Therapist" -> listOf(
                ConsultationModel("1","Dr. John Doe", "Therapist", R.drawable.profile_image),
                ConsultationModel("2","Dr. Jane Smith", "Therapist", R.drawable.profile_image)
            )
            "Surgeon" -> listOf(
                ConsultationModel("1","Dr. Sarah Williams", "Surgeon", R.drawable.profile_image),
                ConsultationModel("22","Dr. Michael Brown", "Surgeon", R.drawable.profile_image)
            )
            "Pediatrician" -> listOf(
                ConsultationModel("122","Dr. Emily Davis", "Pediatrician", R.drawable.profile_image),
                ConsultationModel("133","Dr. Robert Johnson", "Pediatrician", R.drawable.profile_image)
            )
            "Neurologist" -> listOf(
                ConsultationModel("44","Dr. David Wilson", "Neurologist", R.drawable.profile_image),
                ConsultationModel("55","Dr. Lisa White", "Neurologist", R.drawable.profile_image)
            )
            else -> emptyList()
        }
    }
}
