package com.cwp.jinja_hub.ui.splash_screen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.ui.onboarding_screens.OnBoardingScreens

class MySplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)




        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, OnBoardingScreens::class.java))
            finish() // Close SplashActivity
        }, 2000) // 3 seconds dela



        val splashContainer = findViewById<RelativeLayout>(R.id.intro_layout)



        // Load the fade-in animation
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        splashContainer.startAnimation(fadeInAnimation)


    }
}