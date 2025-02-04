package com.cwp.jinja_hub.ui.market_place

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.databinding.ActivityMarketPlaceBinding
import com.cwp.jinja_hub.ui.market_place.buy.Buy
import com.cwp.jinja_hub.ui.market_place.sell.CreateADFragment

class MarketPlace : AppCompatActivity() {
    private lateinit var binding: ActivityMarketPlaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMarketPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buy.setOnClickListener {
            // Open buy fragment
            supportFragmentManager.beginTransaction()
                .add(R.id.market_fragment_container, Buy())
                .addToBackStack(null)
                .commit()
        }

        binding.sell.setOnClickListener {
            // open Create ad fragment
            supportFragmentManager.beginTransaction()
                .add(R.id.market_fragment_container, CreateADFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.back.setOnClickListener {
            finish()
        }
    }
}