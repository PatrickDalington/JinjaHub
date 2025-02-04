package com.cwp.jinja_hub.ui.profile.coming_soon

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentComingSoonBinding

class ComingSoonFragment : Fragment() {

    private lateinit var _binding : FragmentComingSoonBinding
    private val binding get() = _binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentComingSoonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get incoming bundle
        val title = arguments?.getString("title")
        binding.title.text = title

        Glide.with(requireActivity()).load(R.drawable.coming_soon).into(binding.comingSoonImage)


        binding.okay.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.layout.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ComingSoonFragment().apply {

            }
    }
}