package com.cwp.jinja_hub.ui.image_viewer

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.image_viewer.ImagesSliderAdapter
import com.cwp.jinja_hub.repository.ReviewRepository
import kotlin.properties.Delegates

class ViewAllImagesActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var name: TextView
    private lateinit var imageCounter: TextView
    private var pos by Delegates.notNull<Int>()

    private val reviewRepository: ReviewRepository = ReviewRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        setContentView(R.layout.activity_view_all_images)


        // Get views from XML
        viewPager = findViewById(R.id.viewPager)
        name = findViewById(R.id.name)
        imageCounter = findViewById(R.id.image_counter)

        // Get the list of image URLs from the Intent
        val imageUrls = intent.getStringArrayListExtra(EXTRA_IMAGE_URLS)
        val posterId = intent.getStringExtra("id")

        if (!imageUrls.isNullOrEmpty()) {
            // Set up the ViewPager2 with the adapter
            val adapter = ImagesSliderAdapter(imageUrls)
            viewPager.adapter = adapter

            // Initialize the image counter
            updateImageCounter(0, imageUrls.size)

            // Set up a listener to update the image counter when swiping
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    pos = position
                    updateImageCounter(position + 1, imageUrls.size)

                }
            })
        } else {
            // If no images are provided, display an error or fallback
            imageCounter.text = "No images available"
        }




        // Get name from the viewModel
        if (posterId != null) {
            reviewRepository.fetchUserDetails(posterId) { fullName, username, profileImage ->
                if (imageUrls!!.size == 1){
                    name.text = "$fullName testimonial image"
                }else if (imageUrls.size > 1) {
                    name.text = "$fullName tesimonial images"
                }
            }
        }
    }

    // Helper function to update the image counter
    private fun updateImageCounter(currentIndex: Int, totalImages: Int) {
        imageCounter.text = "$currentIndex/$totalImages"
    }

    companion object {
        const val EXTRA_IMAGE_URLS = "extra_image_urls"
    }


}
