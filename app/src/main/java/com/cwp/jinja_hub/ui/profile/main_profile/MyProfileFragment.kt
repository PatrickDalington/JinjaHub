package com.cwp.jinja_hub.ui.profile.main_profile

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentMyProfileBinding
import com.cwp.jinja_hub.ui.client_registration.Login
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import com.cwp.jinja_hub.ui.profile.UserProfileViewModel
import com.cwp.jinja_hub.ui.profile.contact_us.ContactUsFragment
import com.cwp.jinja_hub.ui.profile.edit_profile.EditProfileFragment
import com.cwp.jinja_hub.ui.profile.faq.FAQFragment
import com.cwp.jinja_hub.ui.profile.password.PasswordFragment
import com.cwp.jinja_hub.ui.profile.security.SecurityFragment
import com.cwp.jinja_hub.ui.single_image_viewer.SingleImageViewer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyProfileFragment : Fragment() {
    private lateinit var _binding: FragmentMyProfileBinding
    private val binding get() = _binding

    private val viewModel: ProfessionalSignupViewModel by viewModels()
    private val viewM: UserProfileViewModel by viewModels()
    private var verify: Boolean = false
    private val fUser = FirebaseAuth.getInstance().currentUser!!
    private var profilePhoto: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragmentManager.setFragmentResultListener("profileImageUpdated", this) { _, bundle ->
            val updatedUri = bundle.getString("updatedProfileImageUri")
            updatedUri?.let {
                profilePhoto = it
                val headerView = requireActivity().findViewById<View>(R.id.my_profile_container)
                val profileImage = headerView.findViewById<ImageView>(R.id.profile_image)

                val fadeIn = AlphaAnimation(0f, 1f).apply {
                    duration = 500
                    fillAfter = true
                }
                profileImage.startAnimation(fadeIn)

                profileImage.load(it)
            }
        }

        parentFragmentManager.setFragmentResultListener("profileTextUpdated", this) { _, bundle ->
            val fullName = bundle.getString("updatedFullName")
            val username = bundle.getString("updatedUsername")
            val address = bundle.getString("updatedAddress")

            val headerView = requireActivity().findViewById<View>(R.id.my_profile_container)
            fullName?.let { headerView.findViewById<TextView>(R.id.name).text = it }
            username?.let { headerView.findViewById<TextView>(R.id.username).text = it }
            address?.let { headerView.findViewById<TextView>(R.id.address).text = it }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        useDarkStatusBar()

        val headerView = requireActivity().findViewById<View>(R.id.my_profile_container)
        val title = headerView.findViewById<TextView>(R.id.title)
        val editProfile = headerView.findViewById<ImageView>(R.id.edit_profile)
        val backButton = headerView.findViewById<ImageView>(R.id.backButton)
        val profileImage = headerView.findViewById<ImageView>(R.id.profile_image)
        val profileName = headerView.findViewById<TextView>(R.id.name)
        val verified = headerView.findViewById<TextView>(R.id.verified)
        val verifyIcon = headerView.findViewById<ImageView>(R.id.verify)

        viewM.fetchUserDetails(fUser.uid) { fullName, userName, imageUrl, isVerified ->
            verify = isVerified
            profilePhoto = imageUrl
            profileImage.load(imageUrl)
            profileName.text = fullName
            verifyIcon.load(if (isVerified) R.drawable.profile_verify else R.drawable.unverified)
            verified.text = if (isVerified) "Verified" else "Not verified"
        }

        profileImage.setOnClickListener {
            if (profilePhoto.isNotEmpty()) {
                val fragment = SingleImageViewer().apply {
                    arguments = Bundle().apply { putString("image_url", profilePhoto) }
                }
            }

        }



        parentFragmentManager.setFragmentResultListener("profileUpdated", this,{ _, _ ->

            viewM.fetchUserDetails(fUser.uid) { fullName, userName, imageUrl, isVerified ->
                run {
                    verify = isVerified
                    profileImage.load(imageUrl)
                    profileName.text = fullName
                    if (isVerified){
                        verifyIcon.load(R.drawable.profile_verify)
                        verified.text = "Verified"
                    }else{
                        verified.text = "Not verified"
                        verifyIcon.setImageResource(R.drawable.unverified)
                    }
                }

            }
        })

        profileImage.setOnClickListener{
            // Open SingleImageViewer fragment...
            val fragment = SingleImageViewer()
            val bundle = Bundle()
            if (profilePhoto.isNotEmpty())
                bundle.putString("image_url", profilePhoto)
            else
                Toast.makeText(requireContext(), "No image found", Toast.LENGTH_SHORT).show()
            }
        }

        title.text = "My Profile"

        editProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.my_profile_container, EditProfileFragment())
                .addToBackStack("EditProfile")
                .commit()
        }

        backButton.setOnClickListener {
           
            parentFragmentManager.popBackStack()
        }

        binding.faqContainer.setOnClickListener { navigateTo(FAQFragment()) }
        binding.securityContainer.setOnClickListener { navigateTo(SecurityFragment()) }
        binding.supportContainer.setOnClickListener { navigateTo(ContactUsFragment()) }
        binding.passwordContainer.setOnClickListener { navigateTo(PasswordFragment()) }
        binding.checkUpdate.setOnClickListener { checkForAppUpdate() }
        binding.logout.setOnClickListener { confirmLogout() }
    }

    private fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.my_profile_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ -> logout() }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(requireContext(), Login::class.java))
        requireActivity().finish()
    }

    private fun checkForAppUpdate() {
        val currentVersionCode = resources.getString(R.string.app_version).toIntOrNull() ?: 0
        val databaseRef = FirebaseDatabase.getInstance().getReference("latestVersionCode/version")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latestVersionCode = snapshot.getValue(Int::class.java) ?: 0
                if (latestVersionCode > currentVersionCode) showUpdateDialog()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showUpdateDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.update_available_title))
            .setMessage(getString(R.string.update_available_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.update_now)) { _, _ -> openPlayStore() }
            .setNegativeButton(getString(R.string.later)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun openPlayStore() {
        val appPackageName = requireActivity().packageName
        val uri = Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun useDarkStatusBar() {
        requireActivity().window.statusBarColor = resources.getColor(R.color.black, requireActivity().theme)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            @Suppress("DEPRECATION")
            requireActivity().window.decorView.systemUiVisibility = 0
        }
    }
}