package com.cwp.jinja_hub.ui.client_registration

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.ActivityLoginBinding
import com.cwp.jinja_hub.repository.LoginRepository
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignUp
import com.cwp.jinja_hub.ui.profile.password.PasswordFragment
import com.cwp.jinja_hub.ui.welcome.Welcome
import com.google.firebase.auth.FirebaseUser

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private val repository: LoginRepository = LoginRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = LoginViewModel.LoginViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]

        binding.signup.setOnClickListener {
            Intent(this, ProfessionalSignUp::class.java).also {
                startActivity(it)
            }
        }

        binding.forgotPassword.setOnClickListener {
            val fragment = PasswordFragment()
            openFragment(fragment)
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

        binding.signInWithGoogle.setOnClickListener {
            Toast.makeText(this, "Sign in with Google (Coming soon) 🧑🏽‍💻", Toast.LENGTH_SHORT).show()
        }


        // Toggle password visibility when clicking on the toggle ImageView
        binding.toggle.setOnClickListener {
            if (binding.password.text.toString().isNotEmpty()){
                // Check if the password is currently hidden
                if (binding.password.transformationMethod is PasswordTransformationMethod) {
                    // Show password and change icon to eye_cl (indicating password is visible)
                    binding.password.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    binding.toggle.setImageResource(R.drawable.eye_cl)
                } else {
                    // Hide password and change icon to eye_open (indicating password is hidden)
                    binding.password.transformationMethod = PasswordTransformationMethod.getInstance()
                    binding.toggle.setImageResource(R.drawable.eye_open)
                }
                // Move the cursor to the end of the text
                binding.password.setSelection(binding.password.text?.length ?: 0)
            }

        }

        observeLoginResult()
        observeLoadingState()
        observeErrorMessage()
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.loginContainer.id, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {
                goToWelcomeActivity(user)
            }
        }
    }

    private fun observeLoadingState() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun observeErrorMessage() {
        viewModel.errorMessage.observe(this) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToWelcomeActivity(user: FirebaseUser) {
        val intent = Intent(this, Welcome::class.java)
        val options = ActivityOptions.makeCustomAnimation(this, R.anim.fade_in, R.anim.fade_out)
        intent.putExtra("uid", user.uid)
        startActivity(intent, options.toBundle())
        finish()
    }
}
