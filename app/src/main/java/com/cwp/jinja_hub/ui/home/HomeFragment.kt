package com.cwp.jinja_hub.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.com.cwp.jinja_hub.listeners.ReselectedListener
import com.cwp.jinja_hub.com.cwp.jinja_hub.ui.jinja_business.JinjaBusinessFragment
import com.cwp.jinja_hub.databinding.FragmentHomeBinding
import com.cwp.jinja_hub.repository.HomeRepository
import com.cwp.jinja_hub.repository.ProfessionalSignupRepository
import com.cwp.jinja_hub.ui.jinja_product.JinjaProduct
import com.cwp.jinja_hub.ui.market_place.MarketPlaceFragment
import com.cwp.jinja_hub.ui.notifications.NotificationsFragment
import com.cwp.jinja_hub.ui.profile.UserProfileFragment
import com.cwp.jinja_hub.ui.testimony_reviews.Reviews
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment() : Fragment(), ReselectedListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var userName: StringBuilder
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var authListener: FirebaseAuth.AuthStateListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val window = requireActivity().window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)

        // Set status bar color
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        // Make status bar icons light (dark text/icons)
        windowInsetsController.isAppearanceLightStatusBars = true // Use `false` for light icons

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initSharedPreferences()
        initAdMob()
        loadAdWithDelay()
        loadUserData()
        setClickListeners()
        observeNotifications()
        setupAuthStateListener()

        // Check verification on fragment start
        FirebaseAuth.getInstance().currentUser?.let { user ->
            updateUserVerificationStatus(user.uid, user)
        }

        val findADoc = binding.findADoc
        val relativeLayout = binding.relativeLayout




        // Check if the animation has already been played
        val animationPlayed = sharedPreferences.getBoolean("animation_played_launch", false)

        if (!animationPlayed) {
            // Initial state (hidden and translated)
            findADoc.alpha = 0f
            findADoc.translationY = 100f // Adjust the translation distance as needed
            relativeLayout.alpha = 0f
            relativeLayout.translationY = 100f

            // Animation for findADoc
            findADoc.visibility = View.VISIBLE // Ensure it's visible before animating
            findADoc.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300) // Adjust duration as needed
                .setInterpolator(AccelerateDecelerateInterpolator()) // Smooth animation
                .withEndAction {
                    // Animation for relativeLayout (starts after findADoc finishes)
                    relativeLayout.visibility = View.VISIBLE
                    relativeLayout.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(500) // Adjust duration as needed
                        .setInterpolator(AccelerateDecelerateInterpolator())

                    // Mark that the animation has been played in this app launch
                    sharedPreferences.edit().putBoolean("animation_played_launch", true).apply()
                }
                .start()
        } else {
            // If the animation has been played, set the views to their final state
            findADoc.visibility = View.VISIBLE
            findADoc.alpha = 1f
            findADoc.translationY = 0f
            relativeLayout.visibility = View.VISIBLE
            relativeLayout.alpha = 1f
            relativeLayout.translationY = 0f
        }

        parentFragmentManager.setFragmentResultListener("profileUpdated", viewLifecycleOwner) { _, bundle ->

       
        }
    }

    private fun initViewModel() {
        val viewModelFactory = HomeViewModel.HomeViewModelFactory(HomeRepository())
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]
    }

    private fun initSharedPreferences() {
        sharedPreferences =
            requireActivity().getSharedPreferences("JinjaHubPrefs", Context.MODE_PRIVATE)
    }

    private fun initAdMob() {
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            try {
                MobileAds.initialize(requireActivity()) { status ->
                    Log.d("AdMob", "AdMob Initialized: $status")
                }
            } catch (e: Exception) {
                Log.e("AdMobError", "Failed to initialize AdMob: ${e.message}")
            }
        }
    }

    private fun loadAdWithDelay() {
        val delayMillis = 3000L // 3 seconds delay (adjust as needed)
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed({
            val adRequest = AdRequest.Builder().build()
            if (_binding != null) {
                binding.adView.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                        println("Ad Loaded")
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        // Code to be executed when an ad request fails.
                        println("Ad failed to load: ${adError.message}")
                    }

                    override fun onAdOpened() {
                        // Code to be executed when an ad opens an overlay that covers the screen.
                        println("Ad Opened")
                    }

                    override fun onAdClicked() {
                        // Code to be executed when the user clicks on an ad.
                        println("Ad Clicked")
                    }

                    override fun onAdClosed() {
                        // Code to be executed when the user is about to return to the app after tapping on an ad.
                        println("Ad Closed")
                    }
                }
                binding.adView.visibility = View.VISIBLE
                binding.adView.animate().apply {
                    duration = 3000
                    alpha(1f)
                }
                binding.adView.loadAd(adRequest)
            }
        }, delayMillis)
    }

    private fun loadUserData() {


        val name: TextView = binding.userName
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        userName = StringBuilder()
        if (currentUser != null) {
            val userId = currentUser.uid
            viewModel.getUserInfo(userId) { user ->
                if (isAdded) {
                    if (user != null) {
                        userName.append("${user.lastName} ${user.firstName}")
                        name.text = userName
                        binding.profileImage.load(user.profileImage)
                    } else {
                        userName.append("User 0")
                        name.text = userName
                    }
                }
            }
        } else {
            userName.append("__user 0__")
            name.text = userName
        }
    }

    private fun setClickListeners() {
        binding.findADoc.setOnClickListener {
            (requireActivity() as MainActivity).selectTabAtPosition(
                1
            )
        }
        binding.cardJinjaMarketplace.setOnClickListener { loadFragment(MarketPlaceFragment()) }
        binding.cardJinjaProduct.setOnClickListener { loadFragment(JinjaProduct()) }
        binding.cardTestimonials.setOnClickListener { loadFragment(Reviews()) }
        binding.cardJinjaBusiness.setOnClickListener { loadFragment(JinjaBusinessFragment()) }
        binding.notification.setOnClickListener { loadFragment(NotificationsFragment()) }
        binding.profileImage.setOnClickListener { loadFragment(UserProfileFragment()) }
    }

    private fun observeNotifications() {
        viewModel.hasUnreadNotifications.observe(viewLifecycleOwner) { hasUnread ->
            binding.newNotification.visibility = if (hasUnread) View.VISIBLE else View.GONE
        }
    }

    private fun setupAuthStateListener() {
        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                updateUserVerificationStatus(user.uid, user)
            }
        }
        FirebaseAuth.getInstance().addAuthStateListener(authListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        FirebaseAuth.getInstance().removeAuthStateListener(authListener)
        _binding = null
    }

    private fun loadFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.home_container, fragment)
            .addToBackStack("UserProfile")
            .commit()
    }

    override fun onTabReselected() {
        loadFragment(HomeFragment())
    }

    private fun isFirstTimeUser(): Boolean = sharedPreferences.getBoolean("isFirstTime", true)

    private fun updateUserVerificationStatus(userId: String, fUser: FirebaseUser) {
        val repository = ProfessionalSignupRepository()
        Log.d("Verification", "User verified status: ${fUser.isEmailVerified}")

        if (fUser.isEmailVerified) {
            // Update user profile if not already marked as verified
            if (!sharedPreferences.getBoolean("isVerified", false)) {
                repository.updateUserProfile(userId, mapOf("isVerified" to true)) { success ->
                    if (success) {
                        Log.d("FCM", "Token updated successfully")
                        sharedPreferences.edit().putBoolean("isVerified", true).apply()
                    } else {
                        Log.e("FCM", "Token update failed")
                    }
                }
            }
        } else {
            // Show verification dialog if not shown before
            if (!sharedPreferences.getBoolean("verification_prompted", false)) {
                showVerificationDialog(repository, userId)
            }
        }
    }

    private fun showVerificationDialog(
        repository: ProfessionalSignupRepository,
        userId: String
    ) {
        val messages = listOf(
            "Your account is not verified. Please check your email for a verification link to activate your account.",
            "It seems your email is unverified. Please verify your email by clicking on the link we sent you.",
            "To secure your account, kindly verify your email address. Look for the verification link in your inbox.",
            "Almost there! Verify your email address using the link we sent to get full access to our features.",
            "Your account remains unverified. Please verify your email to continue enjoying our services."
        )
        val message = messages.random()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Account not verified")
            .setMessage(message)
            .setPositiveButton("Proceed") { _, _ ->
                showVerificationInfoDialog(repository, userId)
            }
            .setNegativeButton("Later") { dialog, _ -> dialog.dismiss() }
            .show()

        // Mark that the user has been prompted
        sharedPreferences.edit().putBoolean("verification_prompted", true).apply()
    }

    private fun showVerificationInfoDialog(
        repository: ProfessionalSignupRepository,
        userId: String
    ) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Info")
            .setMessage("We are about to send you a verification link to your registered email. Please check your inbox.\n\nIf you are confused about your registered email, please check your profile before proceeding with verification.")
            .setPositiveButton("Verify") { _, _ ->
                repository.sendVerificationLink(userId) { success ->
                    showVerificationResultDialog(success)
                }
            }
            .setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        sharedPreferences.edit().putBoolean("animation_played_launch", false).apply()

    }





    private fun showVerificationResultDialog(success: Boolean) {
        val title = if (success) "Verification Link Sent" else "Failed to Send Link"
        val message = if (success) {
            "A verification link has been sent to your email. Please check your inbox.\n\nThe link expires in 2 minutes."
        } else {
            "Failed to send verification link. Check your internet connection."
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }





}