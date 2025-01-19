package com.cwp.jinja_hub.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.cwp.jinja_hub.MainActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentHomeBinding
import com.cwp.jinja_hub.repository.HomeRepository
import com.cwp.jinja_hub.ui.jinja_product.JinjaProduct
import com.cwp.jinja_hub.ui.market_place.MarketPlace
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    private lateinit var userName: StringBuilder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize ViewModel
        val viewModelFactory = HomeViewModel.HomeViewModelFactory(HomeRepository())
        viewModel = ViewModelProvider(this, viewModelFactory)[HomeViewModel::class.java]

        // Get references from binding
        val name: TextView = binding.userName

        // Firebase Authentication
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        userName = StringBuilder()
        if (currentUser != null) {
            val userId = currentUser.uid

            viewModel.getUserInfo(userId) { user ->
                if (user != null) {
                    userName.append("${user.lastName} ${user.firstName}")
                    name.text = userName
                    binding.profileImage.load(user.profileImage)
                } else {
                    userName.append("User 0")
                    name.text = userName
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
            Intent(requireContext(), MarketPlace::class.java).also {
                startActivity(it)
            }
        }

        binding.cardJinjaProduct.setOnClickListener{
            // Open jinjaProduct fragment
            loadFragment(JinjaProduct())
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Avoid memory leaks
    }

    private fun loadFragment(fragment: Fragment){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.home_container, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }
}
