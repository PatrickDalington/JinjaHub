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
    private val a : String = "Consultation"
    private val c : String = "Appointment"
    private val allCards = mapOf(
        "Therapist" to listOf(
            CardItem(1, "Therapist $c", "Therapist",R.drawable.doc),
            CardItem(2, "Therapist $a", "Therapist",R.drawable.doc)
        ),
        "Surgeon" to listOf(
            CardItem(1, "Surgery $c", "Surgeon", R.drawable.doc),
            CardItem(2, "Surgeon $a", "Surgeon", R.drawable.doc)
        ),
        "Pediatrician" to listOf(
            CardItem(1, "Pediatric $c", "Pediatrician", R.drawable.doc),
            CardItem(2, "Pediatric $a", "Pediatrician", R.drawable.doc)
        ),
        "Neurologist" to listOf(
            CardItem(1, "Neurology $c", "Neurologist", R.drawable.doc),
            CardItem(2, "Neurology $a", "Neurologist", R.drawable.doc)
        ),
        "Cardiologist" to listOf(
            CardItem(1, "Cardiologist $c", "Cardiologist", R.drawable.doc),
            CardItem(2, "Cardiologist $a", "Cardiologist", R.drawable.doc)
        ),
        "Dermatologist" to listOf(
            CardItem(1, "Dermatologist $c", "Dermatologist", R.drawable.doc),
            CardItem(2, "Dermatologist $a", "Dermatologist", R.drawable.doc)
        ),
        "Gynecologist" to listOf(
            CardItem(1, "Gynecology $c", "Gynecologist",R.drawable.doc),
            CardItem(2, "Gynecology $a", "Gynecologist", R.drawable.doc)
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

