package com.cwp.jinja_hub.ui.message

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.cwp.jinja_hub.databinding.FragmentImagePreviewBottomSheetBinding
import com.cwp.jinja_hub.databinding.ImagePreviewItemBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImagePreviewBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentImagePreviewBottomSheetBinding? = null
    private val binding get() = _binding!!
    private var imageUris: MutableList<Uri> = mutableListOf() // Change to MutableList
    var onImagesSendClicked: ((List<Uri>) -> Unit)? = null

    companion object {
        private const val ARG_IMAGE_URIS = "imageUris"
        fun newInstance(imageUris: List<Uri>): ImagePreviewBottomSheetFragment {
            val fragment = ImagePreviewBottomSheetFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_IMAGE_URIS, ArrayList(imageUris))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            it.getParcelableArrayList<Uri>(ARG_IMAGE_URIS)?.toList()?.let { uris ->
                imageUris.addAll(uris) // Initialize imageUris
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImagePreviewBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ImagePreviewAdapter(imageUris) // pass imageUris to adapter
        binding.previewRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.previewRecyclerView.adapter = adapter

        binding.sendButton.setOnClickListener {
            onImagesSendClicked?.invoke(imageUris)
            dismiss()
        }

        binding.close.setOnClickListener {
            // Clear all images
            imageUris.clear()
            adapter.updateData(imageUris)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ImagePreviewAdapter(private val uris: MutableList<Uri>) :
    androidx.recyclerview.widget.ListAdapter<Uri, ImagePreviewAdapter.ImageViewHolder>(
        UriDiffCallback
    ) {

    class ImageViewHolder(val binding: ImagePreviewItemBinding) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    object UriDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ImagePreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = uris[position]
        holder.binding.previewImageView.load(uri)

        holder.binding.close.setOnClickListener {
            uris.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateData(newUris: List<Uri>) {
        uris.clear()
        uris.addAll(newUris)
        submitList(uris.toList())
    }

    override fun getItemCount(): Int = uris.size
}