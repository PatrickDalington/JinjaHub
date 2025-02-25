package com.cwp.jinja_hub.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), ReselectedListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var userName: StringBuilder
    private lateinit var sharedPreferences: SharedPreferences
    private var verificationDialogShown = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initSharedPreferences()
        initAdMob()
        loadAd()
        loadUserData()
        setClickListeners()
        observeNotifications()
        showBetaTesterAlert()
        delayVerificationCheck(view)
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

    private fun loadAd()
    {
        // Load an ad
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    private fun loadUserData()
    {
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

    private fun showBetaTesterAlert() {
        if (isFirstTimeUser()) {
            binding.root.postDelayed({
                if (isAdded && !requireActivity().isFinishing) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Beta Testing (Phase 1) 👋🏻")
                        .setMessage("Thank you for taking your time to test Jinja-Hub v1.\nYour feedback will go a long way to improving the app.")
                        .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                        .show()
                    sharedPreferences.edit().putBoolean("isFirstTime", false).apply()
                }
            }, 500)
        }
    }

    private fun delayVerificationCheck(view: View) {
        val delayMillis = (5000 + Math.random() * 5000).toLong()
        view.postDelayed({
            if (isAdded) FirebaseAuth.getInstance().currentUser?.let {
                updateUserVerificationStatus(
                    it.uid,
                    it
                )
            }
        }, delayMillis)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.home_container, fragment)
            .addToBackStack(null)
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
            repository.updateUserProfile(userId, mapOf("isVerified" to true)) { success ->
                if (success) Log.d("FCM", "Token updated successfully") else Log.e(
                    "FCM",
                    "Token update failed"
                )
            }
        } else {
            val messages = listOf(
                "Your account is not verified. Please check your email for a verification link to activate your account.",
                "It seems your email is unverified. Please verify your email by clicking on the link we sent you.",
                "To secure your account, kindly verify your email address. Look for the verification link in your inbox.",
                "Almost there! Verify your email address using the link we sent to get full access to our features.",
                "Your account remains unverified. Please verify your email to continue enjoying our services."
            )
            val message = messages.random()

            if (!verificationDialogShown && Math.random() < 0.5) {
                verificationDialogShown = true
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Account not verified")
                    .setMessage(message)
                    .setPositiveButton("Proceed") { _, _ ->
                        showVerificationInfoDialog(
                            repository,
                            userId
                        )
                    }
                    .setNegativeButton("Later") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }
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