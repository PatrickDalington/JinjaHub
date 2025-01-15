package com.cwp.jinja_hub.ui.client_registration

import OTPVerificationViewModel
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.ActivityOtpverificationBinding
import com.cwp.jinja_hub.repository.OTPVerificationRepository

class OTPVerification : AppCompatActivity() {
    private lateinit var binding: ActivityOtpverificationBinding
    private lateinit var viewModel: OTPVerificationViewModel

    private var otpSent: Boolean = false // Track if OTP is sent
    private lateinit var resendTimer: CountDownTimer
    private val initialTimerTime: Long = 60000 // 60 seconds for countdown

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpverificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        val repository = OTPVerificationRepository() // Update this if repository takes arguments
        viewModel = ViewModelProvider(
            this,
            OTPVerificationViewModelFactory(repository)
        )[OTPVerificationViewModel::class.java]

        // List of OTP EditTexts
        val otpInputs = listOf(binding.etCode1, binding.etCode2, binding.etCode3, binding.etCode4)

        // Add TextWatcher for OTP inputs
        otpInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && index < otpInputs.size - 1) {
                        otpInputs[index + 1].requestFocus()
                    } else if (s?.isEmpty() == true && index > 0) {
                        otpInputs[index - 1].requestFocus()
                    }
                    viewModel.setOtpCode(index, s.toString())
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }

        // Confirm button click listener
        binding.confirm.setOnClickListener {
            binding.progressBar.visibility = View.VISIBLE
            if (otpSent) {
                val isCodeValid = viewModel.confirmCodeComplete()

                if (isCodeValid) {
                    // Observe the LiveData for the confirmation result
                    viewModel.isCodeConfirmed.observe(this) { isConfirmed ->
                        if (isConfirmed) {
                            Toast.makeText(this, "Verification successful!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Invalid or incomplete code.", Toast.LENGTH_SHORT).show()
                        }
                        binding.progressBar.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(this, "Please complete the OTP code.", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            } else {
                resendOtp()
                binding.confirm.text = "Send Code Again"
                Toast.makeText(this, "Code resent successfully.", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
        }

        // Observe isCodeConfirmed LiveData
        viewModel.isCodeConfirmed.observe(this) { isConfirmed ->
            binding.progressBar.visibility = View.GONE
            if (isConfirmed) {
                Toast.makeText(this, "Code confirmed! Proceeding...", Toast.LENGTH_SHORT).show()
                // Navigate to the next screen here
            } else {
                otpInputs.forEach { it.error = "Invalid code" }
            }
        }

        // Enable confirm button only when the OTP is complete
        viewModel.otpCode.observe(this) { otp ->
            binding.confirm.isEnabled = otp.length == otpInputs.size
        }

        // Calling the startResendTimer function
        startResendTimer()
    }

    private fun startResendTimer() {
        // Set initial text for the countdown
        binding.l.findViewById<TextView>(R.id.time).text = "1:00"

        // Create a countdown timer to update the timer TextView
        resendTimer = object : CountDownTimer(initialTimerTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.l.findViewById<TextView>(R.id.time).text = String.format("00:%02d", secondsRemaining)
            }

            override fun onFinish() {
                // When the timer finishes, reset the button text
                otpSent = false
                binding.confirm.text = "Send Code Again"
                binding.l.findViewById<TextView>(R.id.time).text = "00:00"
            }
        }.start()
    }


    private fun resendOtp() {
        // Handle OTP resend logic (Make API call, etc.)
        Toast.makeText(this, "Sending OTP again...", Toast.LENGTH_SHORT).show()

        // For now, simulate successful OTP sending after a delay
        otpSent = true
        binding.confirm.text = "Confirm"
        startResendTimer()  // Restart the timer after sending the OTP
    }




    override fun onStart() {
        super.onStart()

        // Start the resend timer onStart if OTP has not been sent
        if (!otpSent) {
            startResendTimer()
        }
    }

    override fun onStop() {
        super.onStop()

        // Cancel the countdown timer if the activity stops
        resendTimer.cancel()
    }

}



class OTPVerificationViewModelFactory(
    private val otpVerificationRepository: OTPVerificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OTPVerificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OTPVerificationViewModel(otpVerificationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}