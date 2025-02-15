package com.cwp.jinja_hub.ui.home

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.com.cwp.jinja_hub.listeners.ReselectedListener
import com.cwp.jinja_hub.databinding.FragmentHomeBinding
import com.cwp.jinja_hub.repository.HomeRepository
import com.cwp.jinja_hub.ui.jinja_product.JinjaProduct
import com.cwp.jinja_hub.ui.market_place.MarketPlaceFragment
import com.cwp.jinja_hub.ui.notifications.NotificationsFragment
import com.cwp.jinja_hub.ui.profile.UserProfileFragment
import com.cwp.jinja_hub.ui.testimony_reviews.Reviews
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment(), ReselectedListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    private lateinit var userName: StringBuilder


    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val viewModelFactory = HomeViewModel.HomeViewModelFactory(HomeRepository())
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]


        sharedPreferences = requireActivity().getSharedPreferences("JinjaHubPrefs", Context.MODE_PRIVATE)



        // Get references from binding
        val name: TextView = binding.userName

        // Firebase Authentication
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


        binding.findADoc.setOnClickListener{
            val mainActivity = requireActivity() as MainActivity
            mainActivity.selectTabAtPosition(1)
        }

        binding.cardJinjaMarketplace.setOnClickListener{
            loadFragment(MarketPlaceFragment())
        }

        binding.cardJinjaProduct.setOnClickListener{
            // Open jinjaProduct fragment
            loadFragment(JinjaProduct())
        }

        binding.cardTestimonials.setOnClickListener{
            // open a Review Fragment
            loadFragment(Reviews())
        }

        binding.cardJinjaBusiness.setOnClickListener{
           Toast.makeText(requireContext(), "Development still in progress 💻", Toast.LENGTH_SHORT).show()
        }


        binding.notification.setOnClickListener{
            // Open notification fragment
            loadFragment(NotificationsFragment())
//            FirebaseAuth.getInstance().signOut().also {
//                Intent(requireContext(), MainActivity::class.java).also {
//                    startActivity(it)
//                }
//
//            }
        }

        binding.profileImage.setOnClickListener{
            // open UserProfileFragment
            loadFragment(UserProfileFragment())
        }


        //Check unread notification

            viewModel.hasUnreadNotifications.observe(viewLifecycleOwner) { hasUnread ->
                if (hasUnread) {
                    binding.newNotification.visibility = View.VISIBLE
                } else {
                    binding.newNotification.visibility = View.GONE
                }

        }


        if (isFirstTimeUser())
            loadAlertForBetaTesters()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

    private fun loadFragment(fragment: Fragment){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.home_container, fragment)
        transaction.addToBackStack(null)
        transaction.isAddToBackStackAllowed
        transaction.commit()
    }

    override fun onTabReselected() {
       loadFragment(HomeFragment())
    }


    private fun loadAlertForBetaTesters() {
        // Delay dialog to ensure it's not dismissed prematurely
        binding.root.postDelayed({
            if (isAdded && !requireActivity().isFinishing) {
                val alertDialog = AlertDialog.Builder(requireContext())
                alertDialog.setTitle("Beta Testing (Phase 1) 👋🏻")
                alertDialog.setMessage("Thank you for taking your time to test Jinja-Hub v1.\nYour feedback will go a long way to improving the app.")
                alertDialog.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.create().show()

                // Mark as seen so it doesn’t show again
                sharedPreferences.edit().putBoolean("isFirstTime", false).apply()
            }
        }, 500) // Delay by 500ms to prevent auto-dismiss
    }

    private fun isFirstTimeUser(): Boolean {
        return sharedPreferences.getBoolean("isFirstTime", true)
    }
}
