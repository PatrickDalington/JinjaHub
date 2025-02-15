package com.cwp.jinja_hub.ui.professionals_registration

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.professionals_selector.SelectionPagerAdapter
import com.cwp.jinja_hub.ui.professionals_registration.fragments.AgeFragment
import com.cwp.jinja_hub.ui.professionals_registration.fragments.GenderFragment
import com.cwp.jinja_hub.ui.professionals_registration.fragments.OtherInfoFragment
import com.cwp.jinja_hub.ui.professionals_registration.fragments.PersonalInfoFragment
import com.cwp.jinja_hub.ui.professionals_registration.fragments.ProfessionFragment

class ProfessionalSignUp : AppCompatActivity() {

    lateinit var viewPager: ViewPager2
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_professional_sign_up)


        val medicalProfessionals = listOf(
            "Surgeon",
            "Physician",
            "Nurse",
            "Pharmacist",
            "Dentist",
            "Occupational Therapist",
            "Physical Therapist",
            "Respiratory Therapist",
            "Speech-Language Pathologist",
            "Psychologist",
            "Psychiatrist",
            "Cardiologist",
            "Dermatologist",
            "Endocrinologist",
            "Gastroenterologist",
            "Neurologist",
            "Oncologist",
            "Ophthalmologist",
            "Orthopedic Surgeon",
            "Pediatrician",
            "Radiologist",
            "Urologist",
            "Anesthesiologist",
            "Pathologist",
            "Emergency Medicine Physician",
            "Family Medicine Physician",
            "Geriatrician",
            "Hematologist",
            "Immunologist",
            "Infectious Disease Specialist",
            "Nephrologist",
            "Pulmonologist",
            "Rheumatologist",
            "Allergist",
            "Audiologist",
            "Chiropractor",
            "Dietitian",
            "Medical Assistant",
            "Medical Coder",
            "Medical Biller",
            "Paramedic",
            "EMT",
            "Home Health Aide",
            "Certified Nursing Assistant (CNA)",
            "Licensed Practical Nurse (LPN)",
            "Midwife",
            "Nurse Practitioner",
            "Physician Assistant",
            "Podiatrist",
            "Veterinarian", // While not strictly human medical, often associated.
            "Genetic Counselor",
            "Biomedical Engineer", // Related field
            "Medical Laboratory Technician" // Related field
        )

        // Fragments to show in ViewPager2
        val fragments = listOf(
            GenderFragment.newInstance(
                "Select your Gender",
                listOf("Male", "Female"),
                "gender"
            ),
            AgeFragment.newInstance(
                "Select age best fit you?",
                listOf("Senior Adult", "Adult", "Middle Age", "Teen"),
                "age"
            ),
            PersonalInfoFragment.newInstance(
                "Personal Information",
                listOf("Full Name", "Username", "Email", "Password"),
                "personal_info"
            ),
            OtherInfoFragment.newInstance(
                "Other Information",
                listOf("Yes", "No"),
                "other_info"
            )

        )



        // Set up ViewPager2 and adapter
        viewPager = findViewById(R.id.viewPager)
        viewPager.isUserInputEnabled = false
        val adapter = SelectionPagerAdapter(this, fragments)
        viewPager.adapter = adapter

    }
}