package com.cwp.jinja_hub.utils

import androidx.navigation.NavController
import com.cwp.jinja_hub.R

class NavigateTo {
    fun navigateToHome(navController: NavController) {
        navController.navigate(R.id.navigation_home)
    }
    fun navigateToServices(navController: NavController) {
        navController.navigate(R.id.navigation_services)
    }
    fun navigateToChat(navController: NavController) {
        navController.navigate(R.id.navigation_chat)

    }

}