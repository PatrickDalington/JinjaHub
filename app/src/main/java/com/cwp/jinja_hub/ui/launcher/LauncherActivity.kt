package com.cwp.jinja_hub.com.cwp.jinja_hub.ui.launcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.cwp.jinja_hub.ui.onboarding_screens.OnBoardingScreens

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get receiverId from intent extras

        val receiverId = intent.getStringExtra("receiverId")

        Log.d("LauncherActivity", "receiverId from intent: $receiverId")

        if (!receiverId.isNullOrEmpty()) {
            // Open MessageActivity with receiverId
            val messageIntent = Intent(this, MessageActivity::class.java)
            messageIntent.putExtra("receiverId", receiverId)
            startActivity(messageIntent)
        } else {
            // If no receiverId, open MainActivity
            val mainIntent = Intent(this, OnBoardingScreens::class.java)
            startActivity(mainIntent)
        }

        finish() // Close LauncherActivity
    }
}
