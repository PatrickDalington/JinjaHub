package com.cwp.jinja_hub.ui.splash_screen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.com.cwp.jinja_hub.helpers.TypeWriterView
import com.cwp.jinja_hub.ui.onboarding_screens.OnBoardingScreens

class MySplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)

        // Start the fade-in animation for the splash container
        val splashContainer = findViewById<RelativeLayout>(R.id.intro_layout)
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        splashContainer.startAnimation(fadeInAnimation)

        // Find the TypeWriterView and start the typewriter animation
        val typewriterText = findViewById<TypeWriterView>(R.id.jinja_text)
        typewriterText.setCharacterDelay(150) // Adjust the delay if needed
        typewriterText.animateText("JINJA-HUB")

        // Delay navigation to the next screen (e.g., onboarding) by 2000ms
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, OnBoardingScreens::class.java))
            finish()
        }, 2000)
    }
}
