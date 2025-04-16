package com.cwp.jinja_hub.com.cwp.jinja_hub.ui.activity.edit

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentEditADBinding
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.model.NotificationModel
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.market_place.ADViewModel
import com.cwp.jinja_hub.ui.market_place.congratulation.SuccessAdUpload
import com.cwp.jinja_hub.ui.notifications.NotificationsViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.DecimalFormat
import java.text.NumberFormat


class EditSoapADFragment : Fragment() {

    private var _binding: FragmentEditADBinding? = null
    private val binding get() = _binding!!

    private lateinit var adViewModel: ADViewModel
    private val selectedImageUris = mutableListOf<Uri>()

    private lateinit var notificationViewModel: NotificationsViewModel

    private lateinit var adType: String


    private var adModel: ADModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris.clear()
            selectedImageUris.addAll(uris)
            if (selectedImageUris.size > 1) {
                val alert = AlertDialog.Builder(requireContext())
                alert.setTitle("Warning")
                alert.setMessage("You can only select an image for this product")
                alert.setPositiveButton("OK") { _, _ ->
                    selectedImageUris.clear()
                }
                alert.create().show()

            }else {
                binding.ivUploadMedia.visibility = View.VISIBLE
                binding.ivUploadMedia.setImageURI(uris.first())
                binding.numOfImages.text = "${selectedImageUris.size} image selected ✅"
            }
        } else {
            binding.ivUploadMedia.visibility = View.GONE
            selectedImageUris.clear()
            binding.numOfImages.text = "No images selected"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View ?{
        // Inflate the layout for this fragment
        _binding = FragmentEditADBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModelFactory = ADViewModel.ADViewModelFactory(ADRepository())
        adViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ADViewModel::class.java]

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }


        arguments?.let {
            adModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getParcelable("ad_model", ADModel::class.java)
            } else {
                it.getParcelable("ad_model")
            }
            adModel?.let { model -> populateFields(model) }
        }


        setupUI()
        setupObservers()


    }

    private fun populateFields(ad: ADModel?) {
        ad?.let {
            binding.productName.text = Editable.Factory.getInstance().newEditable(it.productName)
            binding.description.setText(it.description)
            binding.amount.text = Editable.Factory.getInstance().newEditable(it.amount)
            binding.ivUploadMedia.visibility = View.VISIBLE
            binding.ivUploadMedia.load(ad.mediaUrl?.get(0))
            binding.numOfImages.text = "${it.mediaUrl?.size} image selected ✅"
            adType = it.adType
            binding.city.text = Editable.Factory.getInstance().newEditable(it.city)
            binding.state.text = Editable.Factory.getInstance().newEditable(it.state)

        }
    }

    private fun setupUI() {
        binding.uploadMedia.setOnClickListener {
            pickImagesLauncher.launch("image/*") // Opens gallery to select images
        }

        binding.btnSubmitReview.setOnClickListener {
            if (validateInputs()) {
                updateReview()
            }
        }

        binding.verifyUser.setOnClickListener {
            val isVerified = binding.verifyUser.tag as? Boolean ?: false

            if (!isVerified) {
                Toast.makeText(requireContext(), "You are not verified", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "You are verified", Toast.LENGTH_SHORT).show()
            }
        }

        // Handling back button press from toolbar
        binding.backButton.setOnClickListener{
            // Go back to previous fragment that loaded this
            parentFragmentManager.popBackStack()
        }

        // Handling back pressed
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        })
    }

    private fun setupObservers() {
        adViewModel.uploadProgress.observe(viewLifecycleOwner) { isUploading ->
            binding.progressBar.visibility = if (isUploading) View.VISIBLE else View.GONE
            binding.btnSubmitReview.isEnabled = !isUploading
        }

        adViewModel.uploadSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                goToCongratulationPage()
                binding.ivUploadMedia.visibility = View.GONE
                clearInputs()
            }
        }

        adViewModel.uploadError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        // Product Type Spinner
       adType = "Iru Soap"




        // Countries Spinner
        // List of all world countries
        val countries = listOf(
            "Select a country",
            "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda",
            "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan",
            "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin",
            "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria",
            "Burkina Faso", "Burundi", "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Central African Republic",
            "Chad", "Chile", "China", "Colombia", "Comoros", "Congo (Congo-Brazzaville)", "Costa Rica",
            "Croatia", "Cuba", "Cyprus", "Czechia", "Democratic Republic of the Congo", "Denmark", "Djibouti",
            "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea",
            "Eritrea", "Estonia", "Eswatini", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "Gambia",
            "Georgia", "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau",
            "Guyana", "Haiti", "Honduras", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq",
            "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati",
            "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein",
            "Lithuania", "Luxembourg", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta",
            "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Monaco",
            "Mongolia", "Montenegro", "Morocco", "Mozambique", "Myanmar (Burma)", "Namibia", "Nauru",
            "Nepal", "Netherlands", "New Zealand", "Nicaragua", "Niger", "Nigeria", "North Korea",
            "North Macedonia", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea",
            "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia",
            "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines",
            "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia",
            "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands",
            "Somalia", "South Africa", "South Korea", "South Sudan", "Spain", "Sri Lanka", "Sudan",
            "Suriname", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand",
            "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan",
            "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States",
            "Uruguay", "Uzbekistan", "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"
        )

        // Set up the adapter
        val countryAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, countries)
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Attach adapter to spinner




        // Listen to price changes
        binding.amount.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    binding.amount.removeTextChangedListener(this)

                    val cleanString = s.toString().replace(",", "")

                    if (cleanString.isNotEmpty()) {
                        try {
                            val formatted = formatNumber(cleanString.toLong())
                            current = formatted
                            binding.amount.setText(formatted)
                            binding.amount.setSelection(formatted.length) // Move cursor to the end
                        } catch (e: NumberFormatException) {
                            e.printStackTrace()
                        }
                    }

                    binding.amount.addTextChangedListener(this)
                }
            }
        })

        // Set current user image and name
        adViewModel.fetchUserDetails(
            FirebaseAuth.getInstance().currentUser?.uid ?: "",
            { fullName, username, profileImage, isVerified ->
                run {
                    binding.profileImage.load(profileImage)
                    binding.name.text = fullName
                    if (isVerified)
                        binding.verifyUser.load(R.drawable.profile_verify)
                    else
                        binding.verifyUser.load(R.drawable.unverified)

                    binding.verifyUser.tag = isVerified
                }
            }
        )
    }


    private fun validateInputs(): Boolean {
        val productName = binding.productName.text.toString().trim()
        val description = binding.description.text.toString().trim()
        val city = binding.city.text.toString().trim()
        val state = binding.state.text.toString().trim()

        val amount = binding.amount.text.toString().trim()



        return when {

            productName.isEmpty() -> {
                binding.productName.error = "Product name is required"
                false
            }
            adType == "Select an option" -> {
                Toast.makeText(requireContext(), "Please select product type", Toast.LENGTH_SHORT).show()
                false
            }
            amount.isEmpty() -> {
                binding.amount.error = "Amount is required"
                false
            }
            city.isEmpty() -> {
                binding.city.error = "Address is required"
                false
            }
            state.isEmpty() -> {
                binding.state.error = "Address is required"
                false
            }

            description.isEmpty() -> {
                binding.description.error = "Description is required"
                false
            }
            else -> true
        }
    }

    private fun updateReview() {
        val posterId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val adId = adModel?.adId
        val description = binding.description.text.toString().trim()
        val productName = binding.productName.text.toString().trim()
        val amount = binding.amount.text.toString().trim()
        val city = binding.city.text.toString().trim()
        val state = binding.state.text.toString().trim()

        val productType = adType


        val ad = ADModel(
            posterId,
            adId,
            productType,
            description,
            city,
            state,
            "",
            amount,
            productType,
            "",
            productName,
            System.currentTimeMillis(),
            listOf()
        )


        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitReview.isEnabled = false

        //adViewModel.uploadAdWithImages(ad, selectedImageUris)
        adModel?.mediaUrl?.let {
            adViewModel.editAd(
                requireContext(),
                adId ?: "",
                productType,
                ad,
                newImages = selectedImageUris,
                imagesToKeep = it,
                callback = object : ADRepository.EditADCallback {
                    override fun onSuccess() {
                        parentFragmentManager.popBackStack()
                        Toast.makeText(requireContext(), "Ad updated successfully!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(exception: Exception) {
                        Toast.makeText(requireContext(), exception.message, Toast.LENGTH_SHORT).show()
                    }

                }
            )
        }


        adViewModel.uploadProgress.observe(viewLifecycleOwner) { isUploading ->
            if (isUploading) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnSubmitReview.isEnabled = false
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnSubmitReview.isEnabled = true
            }
        }

        adViewModel.uploadSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                val notification = NotificationModel(
                    posterId = posterId,
                    content = "Your ad has been uploaded successfully!",
                    isRead = false,
                    timestamp = System.currentTimeMillis()
                )
                sendNotification(FirebaseAuth.getInstance().currentUser!!.uid, notification)
                Toast.makeText(requireContext(), "Your product has been updated successfully!", Toast.LENGTH_SHORT).show()
                clearInputs()
            } else {
                Toast.makeText(requireContext(), "Failed to upload review.", Toast.LENGTH_SHORT).show()
            }
        }

        adViewModel.uploadError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendNotification(id: String, notification: NotificationModel) {
        notificationViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        notificationViewModel.sendNotification(id, notification)
    }


    private fun clearInputs() {
        binding.productName.text.clear()
        binding.description.text.clear()
        binding.city.text.clear()
        binding.state.text.clear()
       // binding.country.setSelection(0)
        binding.amount.text.clear()
        //binding.phone.text.clear()
        binding.ivUploadMedia.setImageURI(null)
        selectedImageUris.clear()
        binding.ivUploadMedia.visibility = View.GONE
        binding.numOfImages.text = "No images selected"
    }

    private fun loadFragment(fragment: Fragment){
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.home_container, fragment)
        transaction.addToBackStack(null)
        transaction.isAddToBackStackAllowed
        transaction.commit()
    }

    private fun goToCongratulationPage(){
        Intent(requireContext(), SuccessAdUpload::class.java).also {
            startActivity(it)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatNumber(number: Long): String {
        val formatter: NumberFormat = DecimalFormat("#,###")
        return formatter.format(number)
    }

    fun removeCommas(input: String): String {
        return input.replace(",", "")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditSoapADFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}