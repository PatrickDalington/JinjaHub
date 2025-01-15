package com.cwp.jinja_hub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cwp.jinja_hub.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get NavHostFragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        // Bottom Navigation View
        val navView: BottomNavigationView = binding.navView

        // Configure top-level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_services, R.id.navigation_activity
            )
        )

        // Attach NavController to BottomNavigationView
        navView.setupWithNavController(navController)
    }

    fun selectTabAtPosition(position: Int) {
        val menuItemId = when (position) {
            0 -> R.id.navigation_home
            1 -> R.id.navigation_services
            2 -> R.id.navigation_activity
            3 -> R.id.navigation_chat
            else -> R.id.navigation_home
        }
        binding.navView.selectedItemId = menuItemId
    }
}
