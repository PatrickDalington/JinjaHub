package com.cwp.jinja_hub.ui.market_place.sell

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentCreateADBinding
import com.cwp.jinja_hub.model.ADModel
import com.cwp.jinja_hub.model.NotificationModel
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.market_place.ADViewModel
import com.cwp.jinja_hub.ui.market_place.congratulation.SuccessAdUpload
import com.cwp.jinja_hub.ui.notifications.NotificationsViewModel
import com.cwp.jinja_hub.ui.testimony_reviews.Reviews
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.text.DecimalFormat
import java.text.NumberFormat

class CreateADFragment : Fragment() {

    private var _binding: FragmentCreateADBinding? = null
    private val binding get() = _binding!!

    private lateinit var adViewModel: ADViewModel
    private val selectedImageUris = mutableListOf<Uri>()

    private lateinit var notificationViewModel: NotificationsViewModel

    private lateinit var adType: String

    private lateinit var currencyType: String

    private var overCharge = false

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
    ): View {
        _binding = FragmentCreateADBinding.inflate(inflater, container, false)

        // Initialize the ViewModel
        val viewModelFactory = ADViewModel.ADViewModelFactory(ADRepository())
        adViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ADViewModel::class.java]

        setupUI()
        setupObservers()

        return binding.root
    }

    private fun setupUI() {
        binding.uploadMedia.setOnClickListener {
            pickImagesLauncher.launch("image/*") // Opens gallery to select images
        }

        binding.btnSubmitReview.setOnClickListener {
            if (validateInputs()) {
                uploadReview()
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
        // Disable amount input until currency is selected
        binding.amount.isEnabled = false

        // initial hint of amount input
        binding.amount.hint = "Enter amount here"


        // Initialize currency type
        currencyType = ""

        adViewModel.uploadProgress.observe(viewLifecycleOwner) { isUploading ->
            binding.progressHolder.visibility = if (isUploading) View.VISIBLE else View.GONE
            slideInProgressHolder()
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


        // Select currency
        val options = listOf("Select currency", "Dollar ($)", "Naira (NGN)")
        val currencyAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, options)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.currencyType.adapter = currencyAdapter

        binding.currencyType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Avoid showing default message when first item is selected
                if (adType == ""){
                    binding.currencyType.setSelection(0)
                    Snackbar.make(binding.root, "Please select product type first", Snackbar.LENGTH_SHORT).show()
                }else{

                    if (position != 0) {
                        binding.amount.setText("")
                        binding.amount.isEnabled = true
                        currencyType = options[position]
                        if (position == 1 && adType == "Jinja Herbal Extract"){
                            binding.amount.hint = "Charge range ($15 - $25)"
                            binding.currencyTv.text = "Amount in Dollar ($)"
                        }else if (position == 1 && adType == "Iru Soap"){
                            binding.currencyTv.text = "Amount in Dollar ($)"
                            binding.amount.hint = "Charge range ($9 - $14)"
                        }else if (position == 2 && adType == "Jinja Herbal Extract"){
                            binding.currencyTv.text = "Amount in Naira (NGN)"
                            binding.amount.hint = "Charge range (₦15,000 - ₦25,000)"
                        }else if (position == 2 && adType == "Iru Soap"){
                            binding.currencyTv.text = "Amount in Naira (NGN)"
                            binding.amount.hint = "Charge range (₦3,000 - ₦10,000)"
                        }
                    }else{
                        currencyType = ""
                    }

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }


        // Product Type Spinner
        val items = listOf("Select an option", "Jinja Herbal Extract", "Iru Soap")
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.productType.adapter = adapter

        binding.productType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Avoid showing default message when first item is selected
                if (position != 0) {
                    adType = items[position]
                    binding.amount.setText("")


                    if (currencyType != "") {

                        if (adType == "Jinja Herbal Extract" && currencyType == "Dollar ($)") {
                            binding.amount.hint = "Charge range ($15 - $25)"
                        }else if (adType == "Iru Soap" && currencyType == "Dollar ($)") {
                            binding.amount.hint = "Charge range ($9 - $14)"

                        }
                        else if (adType == "Jinja Herbal Extract" && currencyType == "Naira (NGN)") {
                            binding.amount.hint = "Charge range (₦15,000 - ₦25,000)"
                        }
                        else if (adType == "Iru Soap" && currencyType == "Naira (NGN)") {
                            binding.amount.hint = "Charge range (₦3,000 - ₦10,000)"
                        }
                        binding.amount.isEnabled = true
                    }

                }else{
                    adType = ""
                    //Toast.makeText(requireContext(), "Please select product type", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }


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
        binding.country.adapter = countryAdapter



        binding.amount.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val newText = s.toString()

                // Prevent unnecessary updates
                if (newText == current) return

                binding.amount.removeTextChangedListener(this) // Remove listener to avoid infinite loop

                val cleanString = newText.replace(",", "")

                if (cleanString.isNotEmpty()) {
                    try {
                        val inputAmount = cleanString.toLong()
                        val formatted = formatNumber(inputAmount)

                        current = formatted
                        binding.amount.setText(formatted)
                        binding.amount.setSelection(formatted.length) // Move cursor to the end

                        // Check if amount is over or under the allowed range
                        if (binding.productType.getItemAtPosition(binding.productType.selectedItemPosition) == "Jinja Herbal Extract")
                        {
                            if (currencyType == "Naira (NGN)"){
                                when {
                                    inputAmount > 25000 -> {
                                        binding.amount.error = "Maximum ₦25,000"
                                        binding.maxCost.visibility = View.VISIBLE
                                        binding.maxCost.text = "Maximum charge is ₦25,000"
                                        overCharge = true
                                    }
                                    inputAmount < 15000 -> { // Fixed: Corrected to ₦15,000
                                        binding.amount.error = "Minimum ₦15,000"
                                        binding.maxCost.visibility = View.VISIBLE
                                        binding.maxCost.text = "Minimum charge is ₦15,000"
                                        overCharge = true
                                    }
                                    else -> {
                                        binding.maxCost.visibility = View.GONE
                                        overCharge = false
                                    }
                                }
                            }else{
                                when {
                                    inputAmount > 25 -> {
                                        binding.amount.error = "Maximum $25"
                                        binding.maxCost.visibility = View.VISIBLE
                                        binding.maxCost.text = "Maximum charge is $25"
                                        overCharge = true
                                    }
                                    inputAmount < 15 -> { // Fixed: Corrected to ₦15,000
                                        binding.amount.error = "Minimum $15"
                                        binding.maxCost.visibility = View.VISIBLE
                                        binding.maxCost.text = "Minimum charge is $15"
                                        overCharge = true
                                    }
                                    else -> {
                                        binding.maxCost.visibility = View.GONE
                                        overCharge = false
                                    }
                                }
                            }
                        }else if (binding.productType.getItemAtPosition(binding.productType.selectedItemPosition) == "Iru Soap")
                        {
                            if (currencyType == "Naira (NGN)"){
                                when {
                                    inputAmount > 10000 -> {
                                        binding.amount.error = "Maximum ₦10,000"
                                        binding.maxCost.visibility = View.VISIBLE
                                        binding.maxCost.text = "Maximum charge is ₦10,000 for a pack of soap"
                                        overCharge = true
                                    }
                                    inputAmount < 2500 -> { // Fixed: Corrected to ₦10,000
                                        binding.amount.error = "Minimum ₦2,500"
                                        binding.maxCost.visibility = View.VISIBLE
                                        binding.maxCost.text = "Minimum charge is ₦2,500 for a pack of soap"
                                        overCharge = true
                                    }
                                    else -> {
                                        binding.maxCost.visibility = View.GONE
                                        overCharge = false
                                    }
                                }
                            }else{
                                when {
                                    inputAmount > 14 -> {
                                        binding.amount.error = "Maximum $14"
                                        binding.maxCost.visibility = View.VISIBLE
                                        binding.maxCost.text = "Maximum charge is $14 for a pack of soap"
                                        overCharge = true
                                    }
                                    inputAmount < 9 -> {
                                        binding.amount.error = "Minimum $9"
                                        binding.maxCost.visibility = View.VISIBLE
                                        binding.maxCost.text = "Minimum charge is $9 for a pack of soap"
                                        overCharge = true
                                    }
                                    else -> {
                                        binding.maxCost.visibility = View.GONE
                                        overCharge = false
                                    }
                                }
                            }
                        }

                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }
                } else {
                    // Handle empty input
                    binding.maxCost.visibility = View.GONE
                    overCharge = false
                }

                binding.amount.addTextChangedListener(this) // Re-add listener only after modifications
            }
        })

        // Set current user image and name
        adViewModel.fetchUserDetails(FirebaseAuth.getInstance().currentUser?.uid ?: "",
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


    private fun slideInProgressHolder() {
        // Measure the view after setting visibility to visible
        binding.progressHolder.measure(
            View.MeasureSpec.makeMeasureSpec(binding.progressHolder.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // Calculate the starting position (above the view)
        val startY = -binding.progressHolder.measuredHeight.toFloat()

        // Set the initial position
        binding.progressHolder.translationY = startY

        // Create the animation
        val animator = ObjectAnimator.ofFloat(
            binding.progressHolder,
            "translationY",
            startY,
            0f // End position (original position)
        )

        animator.duration = 500 // Adjust duration as needed
        animator.interpolator = AccelerateDecelerateInterpolator()

        // Start the animation
        animator.start()
    }



    private fun validateInputs(): Boolean {
        val productName = binding.productName.text.toString().trim()
        val description = binding.etReviewDescription.text.toString().trim()
        val city = binding.city.text.toString().trim()
        val state = binding.state.text.toString().trim()
        val country = binding.country.selectedItem.toString().trim()
        val amount = binding.amount.text.toString().trim()
        val phone = binding.phone.text.toString().trim()


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
            country == "Select a country" -> {
                Toast.makeText(requireContext(), "Please select country", Toast.LENGTH_SHORT).show()
                false
            }
            description.isEmpty() -> {
                binding.etReviewDescription.error = "Description is required"
                false
            }
            selectedImageUris.isEmpty() -> {
                Toast.makeText(requireContext(), "Please select at least one image", Toast.LENGTH_SHORT).show()
                false
            }
            overCharge -> {
                Toast.makeText(requireContext(), "Amount in or out of range", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun uploadReview() {
        val posterId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val adId = null
        val description = binding.etReviewDescription.text.toString().trim()
        val productName = binding.productName.text.toString().trim()
        val amount = binding.amount.text.toString().trim()
        val city = binding.city.text.toString().trim()
        val state = binding.state.text.toString().trim()
        val country = binding.country.selectedItem.toString().trim()
        val phone = binding.phone.text.toString().trim()
        val productType = adType



        val ad = ADModel(
            posterId,
            adId,
            productType,
            description,
            city,
            state,
            country,
            amount,
            currencyType,
            phone,
            productName,
            System.currentTimeMillis(),
            listOf()
        )

        adViewModel.uploadAdWithImages(ad, selectedImageUris)

        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmitReview.isEnabled = false

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
                Toast.makeText(requireContext(), "Review uploaded successfully!", Toast.LENGTH_SHORT).show()
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
        binding.etReviewDescription.text.clear()
        binding.city.text.clear()
        binding.state.text.clear()
        binding.country.setSelection(0)
        binding.amount.text.clear()
        binding.phone.text.clear()
        binding.productType.setSelection(0)
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
}

