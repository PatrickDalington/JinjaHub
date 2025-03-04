package com.cwp.jinja_hub.ui.market_place.details

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.transition.Visibility
import coil.load
import com.bumptech.glide.Glide
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.ActivityProductDetailBinding
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.market_place.ADViewModel
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.google.firebase.auth.FirebaseAuth

class ProductDetail : AppCompatActivity() {

    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var viewModel: ADViewModel

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Initialize the ViewModel
        val viewModelFactory = ADViewModel.ADViewModelFactory(ADRepository())
        viewModel = ViewModelProvider(this, viewModelFactory)[ADViewModel::class.java]


        // Log all received intent extras
        val extras = intent.extras
        if (extras != null) {
            for (key in extras.keySet()) {
                android.util.Log.d("ProductDetail", "Intent Extra: $key = ${extras.get(key)}")
            }
        }

        // Get intent data safely
        val adId = intent.getStringExtra("adId")
        val adType = intent.getStringExtra("adType")

        if (adId.isNullOrEmpty() || adType.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid ad details", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if data is missing
            return
        }

        android.util.Log.d("ProductDetail", "Received adId: $adId, adType: $adType")

        // Set image to the animated loader
        // Load gif image with glide to the loading product progress bar
        Glide.with(this).load(R.drawable.loading_product).into(binding.loader)


        viewModel.fetchSpecificClickedAD(adId, adType, { isLoading ->
            // Update your UI based on the loading state.
            binding.loader.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.layout.visibility =  View.GONE
            binding.productImage.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.loadingProductDetails.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.message.isEnabled = false

        }, { ad ->

                // setting poster id from ad object to the global variable userId
                userId = ad.posterId

                binding.name.text = ad.productName
                binding.productImage.load(ad.mediaUrl?.get(0))
                binding.description.text = ad.description
                binding.amount.text = ad.amount
                binding.city.text = ad.city
                binding.stateCountry.text = "${ad.state}, ${ad.country}"
                //binding.city.text = ad.city
                //binding.stateAndCountry.text = "${ad.state}, ${ad.country}"

                viewModel.fetchUserDetails(ad.posterId) { fullName, username, profileImage, _ ->
                    //binding.posterName.text = fullName
                    //binding.profileImage.load(profileImage)
                }
                binding.layout.visibility = View.VISIBLE
                binding.message.isEnabled = true

        })

        viewModel.errorMessageLiveData.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }


        // Setting up message button
        binding.message.setOnClickListener {
            if (userId != FirebaseAuth.getInstance().currentUser?.uid) {
                Intent(this, MessageActivity::class.java).apply {
                    putExtra("receiverId", userId)
                    putExtra("comingFrom", "market_place")
                    it.context.startActivity(this)
                }
            }else{
                Toast.makeText(this, "You cannot message yourself", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
