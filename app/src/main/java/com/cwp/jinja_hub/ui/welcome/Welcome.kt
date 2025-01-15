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

class Welcome : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var viewModel: WelcomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get incoming intent
        val name = intent.getStringExtra("firstName")
        val userId = intent.getStringExtra("userId")

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[WelcomeViewModel::class.java]

        // Set the name in the ViewModel
        if (name != null) {
            viewModel.setName(name)
        }

        // Set the name in the UI
        //binding.name.text = viewModel.getName()

        // Observe the name in the ViewModel
        viewModel.name.observe(this) { newName ->
            binding.name.text = "Hey $newName,"
        }

        binding.go.setOnClickListener{
            Intent(this, MainActivity::class.java).also {
                it.putExtra("userId", userId)
                startActivity(it)
            }
        }


    }
}