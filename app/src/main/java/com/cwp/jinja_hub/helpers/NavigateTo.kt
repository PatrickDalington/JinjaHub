package com.cwp.jinja_hub.helpers

import androidx.navigation.NavController
import com.cwp.jinja_hub.R

class NavigateTo {
    fun navigateToHome(navController: NavController) {
        navController.navigate(R.id.navigation_home)
    }
    fun navigateToMarket(navController: NavController) {
        navController.navigate(R.id.navigation_market_place)
    }
    fun navigateToChat(navController: NavController) {
        navController.navigate(R.id.navigation_chat)

    }

}