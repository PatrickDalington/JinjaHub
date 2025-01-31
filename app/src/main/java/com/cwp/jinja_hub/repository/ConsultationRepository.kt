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
            ConsultationModel("08zjWqKB1eR529Z5wChrcMjGthi1", "Dr. John", "Therapist", R.drawable.profile_image),
            ConsultationModel("QKCDMSsbTKX5O4Y1IIYr9DQrkdJ2","Dr. Patrick", "Therapist", R.drawable.profile_image),
            ConsultationModel("AueW1cmzkURPL2cdGX5NNSoxhn02","Dr. Micheal", "Therapist", R.drawable.profile_image),
            ConsultationModel("4","Dr. Dorothy", "Therapist", R.drawable.profile_image),
            ConsultationModel("5","Dr. Jackson", "Therapist", R.drawable.profile_image),
            ConsultationModel("6","Dr. Jane Smith", "Surgeon", R.drawable.profile_image),
            ConsultationModel("7","Dr. Michael Johnson", "Pediatrician", R.drawable.profile_image),
            ConsultationModel("8","Dr. Sarah Williams", "Neurologist", R.drawable.profile_image),
            ConsultationModel("9","Dr. David Brown", "Cardiologist", R.drawable.profile_image),
            ConsultationModel("10","Dr. Emily Davis", "Dermatologist", R.drawable.profile_image),
            ConsultationModel("11","Dr. Robert Miller", "Gynecologist", R.drawable.profile_image)
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