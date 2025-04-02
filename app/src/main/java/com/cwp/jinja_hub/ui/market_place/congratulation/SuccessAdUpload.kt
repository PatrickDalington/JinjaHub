package com.cwp.jinja_hub.ui.market_place.congratulation

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.ActivitySuccessAdUploadBinding
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.market_place.ADViewModel
import com.google.firebase.auth.FirebaseAuth

class SuccessAdUpload : AppCompatActivity() {
    private lateinit var binding: ActivitySuccessAdUploadBinding
    private lateinit var adViewModel: ADViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuccessAdUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // initialize viewModel
        // Initialize the ViewModel
        val viewModelFactory = ADViewModel.ADViewModelFactory(ADRepository())
        adViewModel = ViewModelProvider(this, viewModelFactory)[ADViewModel::class.java]


        // Set current user image and name
        adViewModel.fetchUserDetails(
            FirebaseAuth.getInstance().currentUser?.uid ?: ""
        ) { fullName, username, profileImage, isVerified ->
            run {
                binding.profileImage.load(profileImage)
                binding.name.text = fullName
                if (isVerified)
                    binding.verifyUser.load(R.drawable.profile_verify)
                else
                    binding.verifyUser.load(R.drawable.unverified)

                binding.verifyUser.tag = isVerified
            }
        }


        binding.done.setOnClickListener {
            // Navigate to the main activity
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

        this.onBackPressedDispatcher.addCallback(this) {
            Intent(this@SuccessAdUpload, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }

    }
}