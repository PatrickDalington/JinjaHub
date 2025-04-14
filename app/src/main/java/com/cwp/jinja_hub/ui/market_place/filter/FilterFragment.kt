package com.cwp.jinja_hub.ui.market_place.filter

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.com.cwp.jinja_hub.app_interface.CountriesInterface
import com.cwp.jinja_hub.databinding.FragmentFilterBinding
import com.cwp.jinja_hub.repository.ADRepository
import com.cwp.jinja_hub.ui.market_place.ADViewModel

class FilterFragment : Fragment(),CountriesInterface {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ADViewModel

    private lateinit var countryList: List<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load world countries into Spinner


        //loadCountries()

        val viewModelFactory = ADViewModel.ADViewModelFactory(ADRepository())
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory)[ADViewModel::class.java]


        viewModel.popularAdSoap.observe(viewLifecycleOwner) { soaps ->

            val countries:List<String> = soaps.map {
                it.country
            }.toHashSet().toList()
            loadCountries(countries)

        }


        viewModel.popularAdDrink.observe(viewLifecycleOwner) { drinks ->

            val countries:List<String> = drinks.map {
                it.country
            }.toHashSet().toList()
            loadCountries(countries)

        }




        // Handle Filter button click
        binding.filter.setOnClickListener {
            sendFilterDataToPreviousFragment()
        }

        // Handle Cancel button click
        binding.cancel.setOnClickListener {
            parentFragmentManager.popBackStack() // Close fragment
        }
    }

    private fun loadCountries(countries: List<String>?) {
        // List of sample countries (Replace this with a real country list)

        countryList = countries!!


        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, countryList)
        binding.country.adapter = adapter
    }

    private fun sendFilterDataToPreviousFragment() {
        val selectedCountry = binding.country.selectedItem.toString()
        val state = binding.state.text.toString().trim()
        val area = binding.area.text.toString().trim()

        if (selectedCountry == "Select Country") {
            Toast.makeText(requireContext(), "Please select a country", Toast.LENGTH_SHORT).show()
            return
        }

        // Pass data back to previous fragment
        val bundle = Bundle().apply {
            putString("selected_country", selectedCountry)
            putString("selected_state", state)
            putString("selected_area", area)

        }

        parentFragmentManager.setFragmentResult("filterRequestKey", bundle)

        // Close the fragment properly
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCountries_Added(countries: List<String>): Void {
        TODO("Not yet implemented")
    }
}
