package com.cwp.jinja_hub.repository

import ConsultationModel
import com.cwp.jinja_hub.R
import kotlinx.coroutines.delay

class ConsultationRepository {

    // Simulates fetching data from an API or database
    suspend fun fetchConsultations(): List<ConsultationModel> {
        // Simulate network delay
        delay(2000) // 2 seconds

        // Simulate a failure scenario
//        val isSuccess = (0..1).random() > 0 // 50% chance of failure
//        if (!isSuccess) {
//            throw Exception("Failed to fetch data from the server.")
//        }


        // Simulate consultation data
        return listOf(
            ConsultationModel("Dr. John", "Therapist", R.drawable.profile_image),
            ConsultationModel("Dr. Patrick", "Therapist", R.drawable.profile_image),
            ConsultationModel("Dr. Micheal", "Therapist", R.drawable.profile_image),
            ConsultationModel("Dr. Dorothy", "Therapist", R.drawable.profile_image),
            ConsultationModel("Dr. Jackson", "Therapist", R.drawable.profile_image),
            ConsultationModel("Dr. Jane Smith", "Surgeon", R.drawable.profile_image),
            ConsultationModel("Dr. Michael Johnson", "Pediatrician", R.drawable.profile_image),
            ConsultationModel("Dr. Sarah Williams", "Neurologist", R.drawable.profile_image),
            ConsultationModel("Dr. David Brown", "Cardiologist", R.drawable.profile_image),
            ConsultationModel("Dr. Emily Davis", "Dermatologist", R.drawable.profile_image),
            ConsultationModel("Dr. Robert Miller", "Gynecologist", R.drawable.profile_image)
        )
    }


    suspend fun loadSpecialistsForCategory(category: String): List<ConsultationModel> {
        // Simulate network delay
        delay(1000)

        // Simulate a failure scenario
//        val isSuccess = (0..1).random() > 0 // 50% chance of failure
//        if (!isSuccess) {
//            throw Exception("Failed to fetch specialists for category: $category")
//        }

        // Get all consultations
        val consultations = fetchConsultations()

        // Filter consultations by category
        return consultations.filter { it.specialty == category }
    }
}