package com.cwp.jinja_hub.ui.client_registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.databinding.ActivityLoginBinding
import com.cwp.jinja_hub.repository.LoginRepository
import com.cwp.jinja_hub.ui.services.ServicesViewModel
import com.cwp.jinja_hub.ui.welcome.Welcome
import com.google.firebase.auth.FirebaseUser

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var  viewModel: LoginViewModel
    private val repository: LoginRepository = LoginRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val viewModelFactory = LoginViewModel.LoginViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]


        binding.signup.setOnClickListener {
            Intent(this, ClientSignup::class.java).also {
                startActivity(it)
            }
        }

        binding.signin.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            viewModel.loginUser(email, password)
        }

        observeLoginResult()
    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {

                goToWelcomeActivity(user)

            } else {
                viewModel.errorMessage.observe(this) { errorMessage ->
                    if (errorMessage != null) {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun goToWelcomeActivity(user: FirebaseUser) {
        val intent = Intent(this, Welcome::class.java)
        intent.putExtra("uid", user.uid)
        startActivity(intent)
    }
}

