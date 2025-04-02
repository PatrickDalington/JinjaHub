package com.cwp.jinja_hub.ui.profile.security.fragment

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentSecurityAlertsBinding
import com.cwp.jinja_hub.databinding.FragmentSecurityBinding
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import com.cwp.jinja_hub.ui.profile.UserProfileViewModel
import com.cwp.jinja_hub.ui.profile.contact_us.ContactUsFragment
import com.cwp.jinja_hub.ui.single_image_viewer.SingleImageViewer
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random


class SecurityAlertsFragment : Fragment() {

    private lateinit var _binding: FragmentSecurityAlertsBinding
    private val binding get() = _binding

    private val viewModel : ProfessionalSignupViewModel by viewModels()

    private val fUser = FirebaseAuth.getInstance().currentUser!!

    private var profilePhoto: String = ""

    private val viewM: UserProfileViewModel by viewModels()


    private var verify: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSecurityAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        useDarkStatusBar()
        simulateProgressBarProgress()

        // Getting views from generic layout holder
        val headerView = requireActivity().findViewById<View>(R.id.security_alerts_container)
        val title = headerView.findViewById<TextView>(R.id.title)
        val editProfile = headerView.findViewById<ImageView>(R.id.edit_profile)
        val backButton = headerView.findViewById<ImageView>(R.id.backButton)
        val profileImage = headerView.findViewById<ImageView>(R.id.profile_image)
        val profileName = headerView.findViewById<TextView>(R.id.name)
        val verified = headerView.findViewById<TextView>(R.id.verified)
        val verifyIcon = headerView.findViewById<ImageView>(R.id.verify)


        // Hide edit profile button for this fragment
        editProfile.visibility = View.GONE


        viewM.fetchUserDetails(fUser.uid) { fullName, userName, imageUrl, isVerified ->
            run {
                verify = isVerified
                profileImage.load(imageUrl)
                profileName.text = fullName

                if (isVerified){
                    verified.text = "Verified"
                    verifyIcon.load(R.drawable.profile_verify)
                }else{
                    verified.text = "Not verified"
                    verifyIcon.setImageResource(R.drawable.unverified)
                }
            }

        }



        binding.done.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        binding.contactUs.setOnClickListener{
            // open contact us fragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.security_alerts_container, ContactUsFragment())
                .addToBackStack(null)
                .commit()
        }

        profileImage.setOnClickListener{
            // Open SingleImageViewer fragment
            val fragment = SingleImageViewer()
            val bundle = Bundle()
            if (profilePhoto.isNotEmpty())
                bundle.putString("image_url", profilePhoto)
            else
                Toast.makeText(requireContext(), "No image found", Toast.LENGTH_SHORT).show()
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.my_profile_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }



        // Set title
        title.text = "Security"

    }

    private fun simulateProgressBarProgress() {
        binding.done.isEnabled = false
        val handler = Handler(Looper.getMainLooper())
        var progress = 0
        val maxProgress = 100
        val delayMillis = 180L // Adjust for speed


        val runnable = object : Runnable {
            override fun run() {
                    progress += Random.nextInt(1, 4) // Simulate random increments
                if (progress > maxProgress) {
                    progress = maxProgress
                }
                binding.progressBar.progress = progress

                // Get the current progress value
                val currentProgress = binding.progressBar.progress
                binding.scanning.text = "Scanning... $currentProgress%"

                if (progress < maxProgress) {
                    handler.postDelayed(this, delayMillis)
                }else{

                    val day = listOf("7", "2", "3", "4").random()
                    binding.resultLayout.visibility = View.VISIBLE
                    binding.progressLayout.visibility = View.GONE
                    binding.correct.animate().apply {
                        duration = 3000
                        rotationYBy(360f)
                    }.start().apply {
                        binding.scanResult.text = "No Unusual account activity detected in the last $day days"
                        binding.done.isEnabled = true
                    }

                }
            }
        }

        handler.postDelayed(runnable, delayMillis)
    }

    private fun useDarkStatusBar(){
        // Set status bar color to black
        requireActivity().window.statusBarColor = resources.getColor(R.color.black, requireActivity().theme)

        // Use light icons (the default) on a dark background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.setSystemBarsAppearance(
                0,  // Clear APPEARANCE_LIGHT_STATUS_BARS to use light icons
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            // Clear the flag for older APIs
            requireActivity().window.decorView.systemUiVisibility = 0
        }
    }
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SecurityAlertsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}