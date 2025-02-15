package com.cwp.jinja_hub.ui.onboarding_screens

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.OnboardingPagerAdapter
import com.cwp.jinja_hub.ui.client_registration.ClientSignup
import com.cwp.jinja_hub.ui.client_registration.Login
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignUp
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
                    // Goto ProfessionalSigUp Activity using intent
                    Intent(this, Login::class.java).also {
                        startActivity(it)
                    }


                }
            }
        }
        viewPager2.adapter = adapter

        // show start button if last layout
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == onboardingScreens.size - 1) {
                    start.visibility = View.VISIBLE
                } else {
                    start.visibility = View.GONE
                }
            }
        })

        start.setOnClickListener{
            Intent(this, Login::class.java).also {
                startActivity(it)
            }
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

    override fun onStart() {
        super.onStart()

        val isLoggedInBefore = checkIfUserLoggedInBefore()
        if (isLoggedInBefore){
            Intent(this, Login::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    private fun checkIfUserLoggedInBefore(): Boolean {
        val sharedPreferences = getSharedPreferences("user_log_preferences", MODE_PRIVATE)
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
}