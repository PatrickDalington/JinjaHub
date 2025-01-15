package com.cwp.jinja_hub.ui.consultation

import ConsultationModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.repository.ClientSignupRepository
import com.cwp.jinja_hub.repository.ConsultationRepository
import kotlinx.coroutines.launch



class ConsultationViewModel(private val consultationRepository: ConsultationRepository) : ViewModel(){


    private val _consultationState = MutableLiveData<ConsultationState>()
    val consultationState: LiveData<ConsultationState> = _consultationState

    fun loadConsultations() {
        _consultationState.value = ConsultationState.Loading
        viewModelScope.launch {
            try {
                val consultations = consultationRepository.fetchConsultations()
                _consultationState.postValue(ConsultationState.Success(consultations))
            } catch (e: Exception) {
                _consultationState.postValue(ConsultationState.Error(e.message ?: "Unknown error occurred fetching consultations"))
            }
        }
    }

    // Fetch specialists based on category
    fun loadSpecialistsForCategory(category: String) {
        viewModelScope.launch {
            _consultationState.postValue(ConsultationState.Loading)
            try {
                val specialists = consultationRepository.loadSpecialistsForCategory(category)
                _consultationState.postValue(ConsultationState.Success(specialists))
            } catch (e: Exception) {
                _consultationState.postValue(ConsultationState.Error(e.message ?: "An unknown error occurred"))
            }
        }
    }


sealed class ConsultationState {
        data object Loading : ConsultationState()
        data class Success(val consultations: List<ConsultationModel>) : ConsultationState()
        data class Error(val message: String) : ConsultationState()
    }
}
