package com.cwp.jinja_hub.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import coil.load
import com.bumptech.glide.Glide
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentUserProfileBinding
import com.cwp.jinja_hub.ui.profile.coming_soon.ComingSoonFragment
import com.cwp.jinja_hub.ui.profile.main_profile.MyProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.properties.Delegates


class UserProfileFragment : Fragment() {

    private lateinit var _binding : FragmentUserProfileBinding
    private val binding get() = _binding

    private val viewModel: UserProfileViewModel by viewModels()

    private lateinit var fUser: FirebaseUser

    private var verify: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fUser = FirebaseAuth.getInstance().currentUser!!
        // initialize all views from layout
        val toolbar = binding.toolbar
        val backButton = binding.back
        val userProfileImage = binding.profileImage
        val name = binding.name
        val verifyUser = binding.verifyUser
        val availableBalance = binding.balance
        val eye = binding.eye
        val transactionHistory = binding.lin2
        val myProfile = binding.myProfile
        val myWallet = binding.myWallet
        val addBank = binding.addBank





        viewModel.fetchUserDetails(fUser.uid) { fullName, userName, profileImage, isVerified ->
            run {
                verify = isVerified
                name.text = fullName
                if (isVerified) {
                    verifyUser.load(R.drawable.profile_verify)
                } else {
                    verifyUser.load(R.drawable.unverified)
                }
                userProfileImage.load(profileImage)
            }

        }


        verifyUser.setOnClickListener {
            if (verify)
                Toast.makeText(requireContext(), "You are verified", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(requireContext(), "You are not verified", Toast.LENGTH_SHORT).show()
        }

        eye.setOnClickListener {
            // toggle text of availableBalance
            if (availableBalance.text.toString() == "******") {
                availableBalance.text = "0.00"
                eye.setImageResource(R.drawable.eyes_closed)
            } else {
                availableBalance.text = "******"
                eye.setImageResource(R.drawable.eye)
            }
        }


        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        myProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.user_profile_container, MyProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        myWallet.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("title", myWallet.text.toString())

            val fragment = ComingSoonFragment()
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.user_profile_container, fragment)
                .addToBackStack(null)
                .commit()
        }


        addBank.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("title", addBank.text.toString())

            val fragment = ComingSoonFragment()
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.user_profile_container, fragment)
                .addToBackStack(null)
                .commit()
        }


        binding.withdraw.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("title", binding.withdraw.text.toString())

            val fragment = ComingSoonFragment()
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.user_profile_container, fragment)
                .addToBackStack(null)
                .commit()
        }



    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserProfileFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}