package com.cwp.jinja_hub.ui.onboarding_screens

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.OnboardingPagerAdapter
import com.cwp.jinja_hub.ui.client_registration.ClientSignup
import com.google.firebase.auth.FirebaseAuth

class OnBoardingScreens : AppCompatActivity() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var start: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_on_boarding_screens)

        // Checking if the user is already signed up
        checkIfUserSignedUp()

        viewPager2 = findViewById(R.id.viewPager2)
        start = findViewById(R.id.get_started)

        val onboardingScreens = listOf(
            R.layout.onboarding_item_one,
            R.layout.onboarding_item_two,
            R.layout.onboarding_item_three,
            R.layout.onboarding_item_four
        )

        val adapter = OnboardingPagerAdapter(onboardingScreens){
                view, _ ->
            when (view.id){
                R.id.client_card -> {
                    Intent(this, ClientSignup::class.java).also {
                        startActivity(it)
                    }
                   // Toast.makeText(this, "Client Registration", Toast.LENGTH_SHORT).show()
                }
                R.id.specialist_card -> {
                    Toast.makeText(this, "Specialist Registration", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewPager2.adapter = adapter

        start.setOnClickListener{
            viewPager2.setCurrentItem(viewPager2.adapter?.itemCount?.minus(1) ?: 0, true)
        }

    }


    private fun checkIfUserSignedUp(){
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Redirect to MainActivity if the user is already logged in
            Intent(this, MainActivity::class.java).also {
                it.putExtra("userId", currentUser.uid)
                startActivity(it)
                finish() // Prevent going back to the onboarding screens
            }
            return
        }
    }
}