package com.cwp.jinja_hub.ui.consultation

import ConsultationModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cwp.jinja_hub.model.ProfessionalUser
import com.cwp.jinja_hub.repository.ConsultationRepository
import kotlinx.coroutines.launch


class ConsultationViewModel(private val consultationRepository: ConsultationRepository) : ViewModel(){


    private val _consultationState = MutableLiveData<ConsultationState>()
    val consultationState: LiveData<ConsultationState> = _consultationState



    // Fetch specialists based on category
    fun loadSpecialistsForCategory(category: String) {
        viewModelScope.launch {
            _consultationState.postValue(ConsultationState.Loading)
            try {
                val specialists = consultationRepository.loadProfessionalsFromFirebase(category)
                _consultationState.postValue(ConsultationState.Success(specialists))
            } catch (e: Exception) {
                _consultationState.postValue(ConsultationState.Error(e.message ?: "An unknown error occurred"))
            }
        }
    }

    // get specialist info
    fun getSpecialistInfo(userId: String, callback: (ProfessionalUser) -> Unit) {
        consultationRepository.getSpecialistUserInfo(userId) { user ->
            if (user != null) {
                callback(user)
            }
        }
    }


sealed class ConsultationState {
        data object Loading : ConsultationState()
        data class Success(val consultations: List<ConsultationModel>) : ConsultationState()
        data class Error(val message: String) : ConsultationState()
    }


    // Factory class for creating MessageViewModel instances
    class ConsultationViewModelFactory(private val repository: ConsultationModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ConsultationViewModelFactory::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ConsultationViewModelFactory(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
