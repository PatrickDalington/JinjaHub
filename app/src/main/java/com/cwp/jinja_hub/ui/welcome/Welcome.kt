package com.cwp.jinja_hub.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.ActivityWelcomeBinding
import com.cwp.jinja_hub.repository.ProfessionalSignupRepository
import com.cwp.jinja_hub.ui.client_registration.Login
import com.google.firebase.auth.FirebaseAuth

class Welcome : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var viewModel: WelcomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        val viewModelFactory = WelcomeViewModel.WelcomeViewModelFactory(ProfessionalSignupRepository())
        viewModel = ViewModelProvider(this, viewModelFactory).get(WelcomeViewModel::class.java)


        // Set the name in the UI
        //binding.name.text = viewModel.getName()

        viewModel.getUserProfile(FirebaseAuth.getInstance().currentUser!!.uid) { user ->
            if (user != null) {
                val fullName = "${user.firstName} ${user.lastName}"
                binding.name.text = "Hey $fullName,"
            }
        }


        binding.go.setOnClickListener{
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }


    }

    override fun onStart() {
        super.onStart()
        val isLoggedInBefore = checkIfUserLoggedInBefore()
        if (isLoggedInBefore){
            binding.welcome.text = "Welcome back\non Board"
        }
    }

    private fun checkIfUserLoggedInBefore(): Boolean {
        val sharedPreferences = getSharedPreferences("user_log_preferences", MODE_PRIVATE)
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
}