package com.cwp.jinja_hub.adapters

import ConsultationModel
import android.os.Bundle
import android.provider.Settings.Global.putInt
import android.provider.Settings.Secure.putString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.ui.specialist_profile.SpecialistProfileFragment

class ConsultationAdapter(private var consultationList: List<ConsultationModel>) :
    RecyclerView.Adapter<ConsultationAdapter.ConsultationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsultationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.consultation_layout_item, parent, false)
        return ConsultationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConsultationViewHolder, position: Int) {
        val specialist = consultationList[position]

        holder.name.text = specialist.name
        holder.occupation.text = specialist.specialty
        holder.image.setImageResource(specialist.imageResId)

        // Handle click events for each specialist
        holder.itemView.setOnClickListener {
            Toast.makeText(
                holder.itemView.context,
                "${specialist.name} (${specialist.specialty}) selected",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Optional: Handle the check icon click
        holder.check.setOnClickListener {
            // Open SpecialistProfileFragment

            val bundle = Bundle().apply {
                putString("specialist_name", specialist.name)
                putString("specialization", specialist.specialty)
                putInt("image_res_id", specialist.imageResId)
            }

            val specialistProfileFragment = SpecialistProfileFragment().apply {
                arguments = bundle
            }

            (holder.itemView.context as? AppCompatActivity)?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.my_container, specialistProfileFragment)
                ?.addToBackStack(null)
                ?.commit()
        }
//            Toast.makeText(holder.itemView.context, "Checking details of ${specialist.name}...", Toast.LENGTH_SHORT).show()
    }


    override fun getItemCount(): Int = consultationList.size

    // Update the consultations list and notify the adapter
    fun updateConsultations(newConsultations: List<ConsultationModel>) {
        consultationList = newConsultations
        notifyDataSetChanged()
    }

    class ConsultationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val occupation: TextView = itemView.findViewById(R.id.occupation)
        val image: ImageView = itemView.findViewById(R.id.profile_image)
        val check: ImageView = itemView.findViewById(R.id.check)
    }
}
