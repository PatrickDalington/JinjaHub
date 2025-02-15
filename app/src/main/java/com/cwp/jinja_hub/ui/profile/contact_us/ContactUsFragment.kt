package com.cwp.jinja_hub.ui.profile.contact_us

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.FragmentContactUsBinding
import com.cwp.jinja_hub.ui.profile.contact_us.fragment.ContactUsMessageFragment


class ContactUsFragment : Fragment() {
    private lateinit var _binding: FragmentContactUsBinding
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
        _binding = FragmentContactUsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.contact.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.contact_us_container, ContactUsMessageFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ContactUsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}