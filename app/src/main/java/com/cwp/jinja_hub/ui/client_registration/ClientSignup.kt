package com.cwp.jinja_hub.ui.client_registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.databinding.ActivityClientSignupBinding
import com.cwp.jinja_hub.ui.welcome.Welcome

class ClientSignup : AppCompatActivity() {

    private lateinit var binding: ActivityClientSignupBinding
    private lateinit var viewModel: ClientSignupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ClientSignupViewModel::class.java]

        binding.toolbar.setNavigationOnClickListener {
            // Handle back navigation
            finish()
        }

        binding.signin.setOnClickListener {
            Intent(this, Login::class.java).also {
                startActivity(it)
            }
        }

        binding.signup.setOnClickListener {
            val fullName = binding.fullName.text.toString()
            val username = binding.username.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.setFullName(fullName)
            viewModel.setUsername(username)
            viewModel.setEmail(email)
            viewModel.setPassword(password)
            viewModel.signup()
        }

        viewModel.isSignupSuccessful.observe(this) { isSuccessful ->
            if (isSuccessful) {
                val userId = viewModel.userId.value
                AlertDialog.Builder(this).apply {
                    setTitle("Hey, ${viewModel.fullName.value}")
                    setMessage("We have sent you a verification code. Please verify your email.")
                    setPositiveButton("OK") { _, _ ->
                        Intent(this@ClientSignup, Welcome::class.java).also {
                            it.putExtra("firstName", viewModel.fullName.value?.split(" ")?.get(0))
                            it.putExtra("userId", userId)
                            startActivity(it)
                        }
                    }
                }.create().show()
            } else {
                Toast.makeText(this, "Sign up failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
