package com.cwp.jinja_hub.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import coil.load
import com.bumptech.glide.Glide
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentUserProfileBinding
import com.cwp.jinja_hub.ui.profile.coming_soon.ComingSoonFragment
import com.cwp.jinja_hub.ui.profile.main_profile.MyProfileFragment
import com.cwp.jinja_hub.ui.profile.settings.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlin.properties.Delegates


class UserProfileFragment : Fragment() {

    private lateinit var _binding : FragmentUserProfileBinding
    private val binding get() = _binding

    private val viewModel: UserProfileViewModel by viewModels()

    private lateinit var fUser: FirebaseUser

    private var verify: Boolean = false

    private lateinit var authListener: FirebaseAuth.AuthStateListener

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



//        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//            val user = firebaseAuth.currentUser
//            if (user != null) {
//                if (user.isEmailVerified){
//                    verifyUser.load(R.drawable.profile_verify)
//                    binding.verifyNow.visibility = View.GONE
//                }else{
//                    verifyUser.load(R.drawable.unverified)
//                    binding.verifyNow.visibility = View.VISIBLE
//                    binding.verifyNow.setCharacterDelay(50)
//                    binding.verifyNow.animateText("Click me to activate your profile")
//                }
//            }
//        }
//        FirebaseAuth.getInstance().addAuthStateListener(authListener)




        viewModel.fetchUserDetails(fUser.uid) { fullName, userName, profileImage, isVerified ->
            run {
                verify = isVerified
                name.text = fullName
                userProfileImage.load(profileImage)

                if (isVerified){
                    verifyUser.load(R.drawable.profile_verify)
                    binding.verifyNow.visibility = View.GONE
                }else{
                    verifyUser.load(R.drawable.unverified)
                    binding.verifyNow.visibility = View.VISIBLE
                    binding.verifyNow.setCharacterDelay(50)
                    binding.verifyNow.animateText("Click me to activate your profile")
                }
            }

        }


        binding.verifyNow.setOnClickListener{
           val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Instruction")
            alertDialog.setMessage("Once you click verify, an email will be sent to your authenticated email. " +
                    "\n\n1. Please click the link to verify your account." +
                    "\n2. Come back and restart your app to complete verification process" +
                    "\n\nIf you are unsure about your registered email, go to profile and check your email." +
                    "\n\nThat's all!!!")
            alertDialog.setPositiveButton("Verify") { _, _ ->
                viewModel.sendVerificationLink(fUser.uid) {
                    if (it) {
                        Toast.makeText(
                            requireContext(),
                            "Verification link sent",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Verification link not sent",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            alertDialog.setNeutralButton("Later") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.create().show()
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
                eye.setImageResource(R.drawable.eye_close)
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

        binding.settings.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("title", binding.settings.text.toString())

            val fragment = SettingsFragment()
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.user_profile_container, fragment)
                .addToBackStack(null)
                .commit()
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove auth listener
        //FirebaseAuth.getInstance().removeAuthStateListener(authListener)
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