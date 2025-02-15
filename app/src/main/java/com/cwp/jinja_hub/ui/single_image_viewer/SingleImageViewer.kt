package com.cwp.jinja_hub.ui.single_image_viewer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import coil.load
import com.cwp.jinja_hub.R
import com.otaliastudios.zoom.ZoomImageView

class SingleImageViewer : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_single_image_viewer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get incoming arguments
        val imageUrl = arguments?.getString("image_url")

        // get views
        val imageViewer = view.findViewById<ZoomImageView>(R.id.image)
        val backButton = view.findViewById<ImageView>(R.id.back)


        // Going back
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Set the image to the image viewer
        imageViewer.load(imageUrl)
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SingleImageViewer().apply {

            }
    }
}