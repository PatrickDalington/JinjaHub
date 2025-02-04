package com.cwp.jinja_hub.adapters.professionals_selector

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R

class ProfSelectionAdapter(
    private val options: List<String>,
    private val onOptionSelected: (String) -> Unit
) : RecyclerView.Adapter<ProfSelectionAdapter.OptionViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.profession_signup_opt_item, parent, false)
        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val option = options[position]
        holder.optionText.text = option
        holder.optionRadioButton.isChecked = position == selectedPosition



        // change background of radio button if isChecked
        if (holder.optionRadioButton.isChecked) {
            holder.optionLayout.setBackgroundResource(R.drawable.radio_button_checked) // Use your checked drawable
            holder.optionText.setTextColor(Color.WHITE)
        } else {
            holder.optionLayout.setBackgroundResource(R.drawable.radio_button_unchecked) // Use your unchecked drawable
            holder.optionText.setTextColor(Color.BLACK)
        }

        // Change optionIcon accordingly
        if (holder.optionText.text == "Male" && holder.optionRadioButton.isChecked){
            holder.optionIcon.setImageResource(R.drawable.male_white)
        }else if (holder.optionText.text == "Male"){
            holder.optionIcon.setImageResource(R.drawable.male_black)
        }else if (holder.optionText.text == "Female" && holder.optionRadioButton.isChecked){
            holder.optionIcon.setImageResource(R.drawable.female_black)
        }else if (holder.optionText.text == "Female"){
            holder.optionIcon.setImageResource(R.drawable.female_black)
        }else if (holder.optionText.text == "Others" && holder.optionRadioButton.isChecked){
            holder.optionIcon.setImageResource(R.drawable.other_gender)
        }else if (holder.optionText.text == "Others"){
            holder.optionIcon.setImageResource(R.drawable.other_gender)
        }else if (holder.optionText.text == "Senior Adult" && holder.optionRadioButton.isChecked){
            holder.optionIcon.setImageResource(R.drawable.male_white)
        }else if (holder.optionText.text == "Senior Adult"){
            holder.optionIcon.setImageResource(R.drawable.male_black)
        }else if (holder.optionText.text == "Adult"){
            holder.optionIcon.setImageResource(R.drawable.female_black)
        }else if (holder.optionText.text == "Middle Age"){
            holder.optionIcon.setImageResource(R.drawable.middle_age_black)
        }else if (holder.optionText.text == "Teen"){
            holder.optionIcon.setImageResource(R.drawable.teen_black)
        }else{
            holder.optionIcon.visibility = View.GONE
        }


        holder.itemView.setOnClickListener {
            selectedPosition = position
            notifyDataSetChanged()
            onOptionSelected(option)
        }
    }

    override fun getItemCount(): Int = options.size

    inner class OptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val optionText: TextView = view.findViewById(R.id.optionTextView)
        val optionRadioButton: RadioButton = view.findViewById(R.id.optionRadioButton)
        val optionIcon: ImageView = view.findViewById(R.id.optionIcon)
        val optionLayout: LinearLayout = view.findViewById(R.id.layout)
    }
}
