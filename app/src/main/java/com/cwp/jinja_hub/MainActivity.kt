package com.cwp.jinja_hub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.cwp.jinja_hub.com.cwp.jinja_hub.listeners.ReselectedListener
import com.cwp.jinja_hub.databinding.ActivityMainBinding
import com.cwp.jinja_hub.ui.testimony_reviews.fragments.comments.LatestCommentsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Get NavHostFragment and NavController.
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        // Bottom Navigation View.
        val navView: BottomNavigationView = binding.navView

//        // Configure top-level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_market_place, R.id.navigation_activity
//            )
//        )

        // Attach NavController to BottomNavigationView.
        navView.setupWithNavController(navController)

        // Handle reselection using the new listener interface.
        navView.setOnItemReselectedListener { menuItem ->
            // Get the current fragment from the NavHostFragment.
            val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
            val currentFragment = navHost.childFragmentManager.primaryNavigationFragment
            if (currentFragment is ReselectedListener) {
                currentFragment.onTabReselected()
            } else {
                Log.d("MainActivity", "Current fragment does not implement ReselectedListener")
            }
        }
    }

    fun selectTabAtPosition(position: Int) {
        val menuItemId = when (position) {
            0 -> R.id.navigation_home
            1 -> R.id.navigation_market_place
            2 -> R.id.navigation_activity
            3 -> R.id.navigation_chat
            else -> R.id.navigation_home
        }
        binding.navView.selectedItemId = menuItemId
    }

    override fun onStart() {
        super.onStart()
        // Set user logged in
        setUserLoggedInBefore()

        // Getting reviewId from DEEP LINK
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get the deep link from the dynamic link
                val deepLink: Uri? = pendingDynamicLinkData?.link
                if (deepLink != null) {
                    // Handle the deep link
                    val reviewId = deepLink.lastPathSegment
                    if (reviewId != null) {
                        Intent(this, LatestCommentsActivity::class.java).apply {
                            putExtra("REVIEW_ID", reviewId)
                            startActivity(this)
                        }
                        // Do something with the reviewId (e.g., open the corresponding review)
                    }
                }
            }
            .addOnFailureListener(this) { e ->
                Log.w("DynamicLinks", "getDynamicLink:onFailure", e)
            }
    }

    private fun setUserLoggedInBefore() {
        val sharedPreferences = getSharedPreferences("user_log_preferences", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
    }
}
