package com.cwp.jinja_hub.ui.profile.edit_profile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentEditProfileBinding
import com.cwp.jinja_hub.ui.professionals_registration.ProfessionalSignupViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {

    private lateinit var _binding: FragmentEditProfileBinding
    private val binding get() = _binding

    private lateinit var viewModel: ProfessionalSignupViewModel
    private lateinit var fUser: FirebaseUser

    // Use a single image picker via GetContent contract.
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Launch a coroutine to update the profile image.
            viewLifecycleOwner.lifecycleScope.launch {
                binding.profileProgressBar.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                Log.d("EditProfileFragment", "Selected image URI: $uri")
                val newImageUri = viewModel.updateUserProfileImage(fUser.uid, uri)
                binding.profileProgressBar.visibility = View.GONE
                if (newImageUri != null) {
                    if (newImageUri.isNotEmpty()) {
                        binding.profileImage.load(newImageUri)
                        Toast.makeText(requireContext(), "Image updated successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    // Request permission launcher for reading external storage (or media images on API 33+).
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            pickImageLauncher.launch("image/*")
        } else {
            Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionsAndPickImage() {
        val permission = getPermissionToUse()
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            pickImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    // Returns the appropriate permission based on the API level.
    private fun getPermissionToUse(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the current Firebase user.
        fUser = FirebaseAuth.getInstance().currentUser!!

        // Initialize the ViewModel.
        viewModel = ViewModelProvider(this)[ProfessionalSignupViewModel::class.java]



        // Observe the progress state.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.progress.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                }
            }
        }

        // Observe error messages.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.errorMessage.collect { errorMsg ->
                    errorMsg?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Back button click listener.
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // When the edit image button is clicked, check for storage permission before launching the image picker.
        binding.editProfileImage.setOnClickListener {
            checkPermissionsAndPickImage()
        }

        // Save button logic: update textual profile fields.
        binding.save.setOnClickListener {
            val fullName = binding.fullName.text.toString().trim()
            val username = binding.username.text.toString().trim()
            val address = binding.address.text.toString().trim()

            // Basic split for first and last names (adjust as needed).
            val nameParts = fullName.split(" ")
            val firstName = if (nameParts.size > 1) nameParts[0] else fullName
            val lastName = if (nameParts.size > 1) nameParts[1] else ""

            val updates = mapOf(
                "fullName" to fullName,
                "firstName" to firstName,
                "lastName" to lastName,
                "username" to username,
                "address" to address
            )

            viewModel.updateUserProfile(fUser.uid, updates)
            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
        }

        // Populate the UI with the current profile data.
        viewModel.getUserProfile(fUser.uid) { profile ->
            profile?.let {
                binding.fullName.setText(it.fullName)
                binding.username.setText(it.username)
                binding.address.setText(it.address)
                binding.profileImage.load(it.profileImage)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditProfileFragment().apply {
                arguments = Bundle().apply {
                    // Optionally add arguments here
                }
            }
    }
}
