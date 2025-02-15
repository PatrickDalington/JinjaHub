package com.cwp.jinja_hub.ui.market_place.buy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.cwp.jinja_hub.model.JinjaDrinkCardItem
import com.cwp.jinja_hub.repository.JinjaMarketRepository

class MyADViewModel: ViewModel() {
    private val repository: JinjaMarketRepository = JinjaMarketRepository()
    private val _cards = MutableLiveData<List<JinjaDrinkCardItem>>()
    val cards: LiveData<List<JinjaDrinkCardItem>> get() = _cards

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _likedMessage = MutableLiveData<String>()
    val likedMessage: LiveData<String> get() = _likedMessage

    init {
        //loadFromLocalStorage()
        loadJinjaDrinks()
    }

    private fun loadJinjaDrinks() {
        _isLoading.value = true
        // Fetch Jinja drinks from Firebase
        repository.fetchJinjaDrinks(
            onSuccess = { drinks ->
                _cards.postValue(drinks.distinctBy { it.id })
                _isLoading.postValue(false)
            },
            onFailure = { exception ->
                _errorMessage.postValue("Failed to fetch Jinja drinks: ${exception.message}")
                _isLoading.postValue(false)
            }
        )
    }


    private fun loadFromLocalStorage(){
        _isLoading.value = true

        // Fetch Jinja drinks from local storage
        repository.generateCards(
            onSuccess = { drinks ->
                _cards.postValue(drinks)
                _isLoading.postValue(false)
            },
            onFailure = { exception ->
                _errorMessage.postValue("Failed to fetch Jinja drinks: ${exception.message}")
                _isLoading.postValue(false)
            }
        )
    }

    fun updateCards(newCards: List<JinjaDrinkCardItem>) {
        _cards.value = newCards
    }



    fun updateLikeStatus(drinkId: String, like: Boolean) {
        repository.likeJinjaDrink(
            drinkId,
            like,
            onSuccess = {
                _likedMessage.value = "Drink liked"
                //loadJinjaDrinks()
                }, // Refresh data
            onFailure = { errorMessage}
        )
    }

}