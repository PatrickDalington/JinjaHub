package com.cwp.jinja_hub.ui.profile

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import coil.request.CachePolicy
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentUserProfileBinding
import com.cwp.jinja_hub.ui.profile.coming_soon.ComingSoonFragment
import com.cwp.jinja_hub.ui.profile.main_profile.MyProfileFragment
import com.cwp.jinja_hub.ui.profile.settings.SettingsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class UserProfileFragment : Fragment() {

    private lateinit var _binding: FragmentUserProfileBinding
    private val binding get() = _binding

    private val viewModel: UserProfileViewModel by viewModels()
    private lateinit var fUser: FirebaseUser

    private var verify: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fUser = FirebaseAuth.getInstance().currentUser!!

        // Listen for image + text updates
        parentFragmentManager.setFragmentResultListener("profileImageUpdated", viewLifecycleOwner) { _, bundle ->
            val updatedUri = bundle.getString("updatedProfileImageUri")
            updatedUri?.let {
                val fadeIn = AlphaAnimation(0f, 1f).apply {
                    duration = 500
                    fillAfter = true
                }
                binding.profileImage.startAnimation(fadeIn)
                binding.profileImage.load(it) {
                    memoryCachePolicy(CachePolicy.DISABLED)
                    diskCachePolicy(CachePolicy.DISABLED)
                    networkCachePolicy(CachePolicy.DISABLED)
                }
            }
        }

        parentFragmentManager.setFragmentResultListener("profileTextUpdated", viewLifecycleOwner) { _, bundle ->
            val fullName = bundle.getString("updatedFullName")
            fullName?.let { binding.name.text = it }
        }


        parentFragmentManager.setFragmentResultListener("profileUpdated", this,{ _, _ ->
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
        })


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
                .setNeutralButton("Later") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        binding.verifyUser.setOnClickListener {
            Toast.makeText(requireContext(), if (verify) "You are verified" else "You are not verified", Toast.LENGTH_SHORT).show()
        }

        binding.eye.setOnClickListener {
            val isHidden = binding.balance.text.toString() == "******"
            binding.balance.text = if (isHidden) "0.00" else "******"
            binding.eye.setImageResource(if (isHidden) R.drawable.eye_close else R.drawable.eye)
        }

        binding.back.setOnClickListener { parentFragmentManager.popBackStack() }

        backButton.setOnClickListener {
            parentFragmentManager.setFragmentResult("profileUpdated",Bundle())
            parentFragmentManager.popBackStack()
        }

        myProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.user_profile_container, MyProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        listOf(
            binding.myWallet to binding.myWallet.text.toString(),
            binding.addBank to binding.addBank.text.toString(),
            binding.withdraw to binding.withdraw.text.toString(),
            binding.settings to binding.settings.text.toString()
        ).forEach { (view, title) ->
            view.setOnClickListener {
                val bundle = Bundle().apply { putString("title", title) }
                val fragment = if (view == binding.settings) SettingsFragment() else ComingSoonFragment()
                fragment.arguments = bundle
                parentFragmentManager.beginTransaction()
                    .replace(R.id.my_profile_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        loadUserDetails()
    }

    private fun loadUserDetails() {
        viewModel.fetchUserDetails(fUser.uid) { fullName, userName, profileImage, isVerified ->
            verify = isVerified
            binding.name.text = fullName
            binding.profileImage.load(profileImage)
            binding.verifyUser.load(if (isVerified) R.drawable.profile_verify else R.drawable.unverified)
            binding.verifyNow.visibility = if (isVerified) View.GONE else View.VISIBLE
            if (!isVerified) {
                binding.verifyNow.setCharacterDelay(50)
                binding.verifyNow.animateText("Click me to activate your profile")
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Toast.makeText(requireContext(), "Attached", Toast.LENGTH_SHORT).show()
    }
}