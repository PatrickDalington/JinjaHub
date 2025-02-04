package com.cwp.jinja_hub.ui.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.model.CardItem
import com.cwp.jinja_hub.model.ServicesCategory
import com.cwp.jinja_hub.repository.ServiceRepository
import kotlinx.coroutines.launch

class ServicesViewModel(private val repository: ServiceRepository) : ViewModel() {

    private val _serviceCategories = MutableLiveData<List<ServicesCategory>?>()
    val categories: MutableLiveData<List<ServicesCategory>?> = _serviceCategories

    private val _cards = MutableLiveData<List<CardItem>>()
    val cards: LiveData<List<CardItem>> = _cards

    // Full list of cards mapped by category
    private val a : String = "Appointment"
    private val c : String = "Consultation"
    private val allCards = mapOf(
        "Anesthesiologist" to listOf(
            CardItem(1, "Pain Management $c", "Anesthesiologist", R.drawable.doc),
            CardItem(2, "Pre-Surgery $a", "Anesthesiologist", R.drawable.clock)
        ),
        "Cardiologist" to listOf(
            CardItem(1, "Heart Health Checkup $c", "Cardiologist", R.drawable.doc),
            CardItem(2, "Cardiac $c", "Cardiologist", R.drawable.clock)
        ),
        "Dentist" to listOf(
            CardItem(1, "Dental Checkup $c", "Dentist", R.drawable.doc),
            CardItem(2, "Teeth Cleaning $a", "Dentist", R.drawable.clock)
        ),
        "Dermatologist" to listOf(
            CardItem(1, "Skin Checkup $c", "Dermatologist", R.drawable.doc),
            CardItem(2, "Acne Treatment $a", "Dermatologist", R.drawable.clock)
        ),
        "Emergency Medicine Physician" to listOf(
            CardItem(1, "Emergency Care $c", "Emergency Medicine Physician", R.drawable.doc),
            CardItem(2, "Trauma Assessment $a", "Emergency Medicine Physician", R.drawable.clock)
        ),
        "Endocrinologist" to listOf(
            CardItem(1, "Diabetes Management $c", "Endocrinologist", R.drawable.doc),
            CardItem(2, "Hormonal Therapy $a", "Endocrinologist", R.drawable.clock)
        ),
        "Family Medicine Physician" to listOf(
            CardItem(1, "Primary Care $c", "Family Medicine Physician", R.drawable.doc),
            CardItem(2, "Routine Checkup $a", "Family Medicine Physician", R.drawable.clock)
        ),
        "Gastroenterologist" to listOf(
            CardItem(1, "Digestive Health $c", "Gastroenterologist", R.drawable.doc),
            CardItem(2, "Colonoscopy $c", "Gastroenterologist", R.drawable.clock)
        ),
        "Geriatrician" to listOf(
            CardItem(1, "Elderly Care $c", "Geriatrician", R.drawable.doc),
            CardItem(2, "Aging Health $c", "Geriatrician", R.drawable.clock)
        ),
        "Hematologist" to listOf(
            CardItem(1, "Blood Disorder Screening $c", "Hematologist", R.drawable.doc),
            CardItem(2, "Iron Deficiency Testing $a", "Hematologist", R.drawable.clock)
        ),
        "Immunologist" to listOf(
            CardItem(1, "Allergy Testing $c", "Immunologist", R.drawable.doc),
            CardItem(2, "Immune System Check $a", "Immunologist", R.drawable.clock)
        ),
        "Medical Laboratory Technician" to listOf(
            CardItem(1, "Blood Test Analysis $c", "Medical Laboratory Technician", R.drawable.doc),
            CardItem(2, "Microbiology Testing $a", "Medical Laboratory Technician", R.drawable.clock)
        ),
        "Neurologist" to listOf(
            CardItem(1, "Brain Health Checkup $c", "Neurologist", R.drawable.doc),
            CardItem(2, "Neurological Testing $a", "Neurologist", R.drawable.clock)
        ),
        "Nephrologist" to listOf(
            CardItem(1, "Kidney Function Test $c", "Nephrologist", R.drawable.doc),
            CardItem(2, "Dialysis $c", "Nephrologist", R.drawable.clock)
        ),
        "Nurse" to listOf(
            CardItem(1, "Nursing $c", "Nurse", R.drawable.doc),
            CardItem(2, "Patient Monitoring $a", "Nurse", R.drawable.clock)
        ),
        "Ophthalmologist" to listOf(
            CardItem(1, "Vision Checkup $c", "Ophthalmologist", R.drawable.doc),
            CardItem(2, "Eye Surgery $c", "Ophthalmologist", R.drawable.clock)
        ),
        "Orthopedic Surgeon" to listOf(
            CardItem(1, "Bone & Joint Care $c", "Orthopedic Surgeon", R.drawable.doc),
            CardItem(2, "Fracture Treatment $a", "Orthopedic Surgeon", R.drawable.clock)
        ),
        "Pathologist" to listOf(
            CardItem(1, "Lab Test Review $c", "Pathologist", R.drawable.doc),
            CardItem(2, "Disease Diagnosis $a", "Pathologist", R.drawable.clock)
        ),
        "Pediatrician" to listOf(
            CardItem(1, "Child Checkup $c", "Pediatrician", R.drawable.doc),
            CardItem(2, "Vaccination $c", "Pediatrician", R.drawable.clock)
        ),
        "Physician" to listOf(
            CardItem(1, "General Checkup $c", "Physician", R.drawable.doc),
            CardItem(2, "Health $c", "Physician", R.drawable.clock)
        ),
        "Physical Therapist" to listOf(
            CardItem(1, "Physical Rehabilitation $c", "Physical Therapist", R.drawable.doc),
            CardItem(2, "Pain Management $a", "Physical Therapist", R.drawable.clock)
        ),
        "Pharmacist" to listOf(
            CardItem(1, "Medication Counseling $c", "Pharmacist", R.drawable.doc),
            CardItem(2, "Prescription Review $a", "Pharmacist", R.drawable.clock)
        ),
        "Psychiatrist" to listOf(
            CardItem(1, "Psychiatric $c", "Psychiatrist", R.drawable.doc),
            CardItem(2, "Medication Management $a", "Psychiatrist", R.drawable.clock)
        ),
        "Psychologist" to listOf(
            CardItem(1, "Mental Health Counseling $c", "Psychologist", R.drawable.doc),
            CardItem(2, "Behavioral Therapy $a", "Psychologist", R.drawable.clock)
        ),
        "Pulmonologist" to listOf(
            CardItem(1, "Lung Function Test $c", "Pulmonologist", R.drawable.doc),
            CardItem(2, "COPD Treatment $a", "Pulmonologist", R.drawable.clock)
        ),
        "Radiologist" to listOf(
            CardItem(1, "Imaging Analysis $c", "Radiologist", R.drawable.doc),
            CardItem(2, "X-Ray $c", "Radiologist", R.drawable.clock)
        ),
        "Respiratory Therapist" to listOf(
            CardItem(1, "Pulmonary Care $c", "Respiratory Therapist", R.drawable.doc),
            CardItem(2, "Breathing Therapy $a", "Respiratory Therapist", R.drawable.clock)
        ),
        "Surgeon" to listOf(
            CardItem(1, "Surgeon $c", "Surgeon", R.drawable.doc),
            CardItem(2, "Surgery $a", "Surgeon", R.drawable.clock)
        ),
        "Urologist" to listOf(
            CardItem(1, "Urinary Health Check $c", "Urologist", R.drawable.doc),
            CardItem(2, "Prostate Screening $a", "Urologist", R.drawable.clock)
        ),
        "Veterinarian" to listOf(
            CardItem(1, "Pet Health Checkup $c", "Veterinarian", R.drawable.doc),
            CardItem(2, "Animal Vaccination $a", "Veterinarian", R.drawable.clock)
        )
    )



    init {
        loadInitialCategories()
    }

    fun loadInitialCategories() {
        // Initialize categories and set the first one as selected
        val initialCategories = allCards.keys.mapIndexed { index, categoryName ->
            ServicesCategory(
                id = index,
                name = categoryName,
                isSelected = index == 0 // The first category is selected by default
            )
        }

        _serviceCategories.value = initialCategories

        // Load cards for the first category by default
        loadCardsForCategory(initialCategories.first())
    }

    fun loadCardsForCategory(category: ServicesCategory) {
        // Fetch the cards associated with the selected category
        val newCards = allCards[category.name] ?: emptyList()

        // Update the card list
        _cards.value = newCards

        // Update the selected category state
        val updatedCategories = _serviceCategories.value?.map {
            it.copy(isSelected = it.name == category.name)
        }
        _serviceCategories.value = updatedCategories
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = repository.fetchCategories()
                _serviceCategories.postValue(categories)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    class ServicesViewModelFactory(private val repository: ServiceRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ServicesViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ServicesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

