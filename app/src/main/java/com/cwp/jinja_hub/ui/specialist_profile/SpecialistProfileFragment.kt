import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.core.view.forEach
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.SpecialistPagerAdapter
import com.cwp.jinja_hub.databinding.FragmentSpecialistProfileBinding
import com.cwp.jinja_hub.repository.ConsultationRepository
import com.cwp.jinja_hub.ui.consultation.ConsultationViewModel
import com.cwp.jinja_hub.ui.consultation.ConsultationViewModelFactory
import com.cwp.jinja_hub.ui.message.MessageActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class SpecialistProfileFragment : Fragment() {

    private var _binding: FragmentSpecialistProfileBinding? = null
    private val binding get() = _binding!!
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var viewModel: ConsultationViewModel
    private lateinit var fUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSpecialistProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fUser = FirebaseAuth.getInstance().currentUser!!
        viewModel = ViewModelProvider(this, ConsultationViewModelFactory(ConsultationRepository()))[ConsultationViewModel::class.java]

        // Retrieve data from arguments
        val specialistId = arguments?.getString("specialist_id")
        val specialistName = arguments?.getString("specialist_name")
        val specialization = arguments?.getString("specialization")


        // setup back button
        binding.back.setOnClickListener{
            requireActivity().onBackPressed()
        }



        // Get specialist info from viewModel
        viewModel.getSpecialistInfo(specialistId.toString()) { user ->
            binding.name.text = user.fullName
            binding.occupation.text = user.medicalProfessional
            binding.profileImage.load(user.profileImage)
            binding.description.text = "@${user.username}\n${user.email}"
            binding.experience.text = "${user.yearsOfWork} years"
            binding.licence.text = user.licence
            binding.placeOfWork.text = user.workplace
        }



        // List of layouts for ViewPager2
        val layouts = listOf(
            R.layout.specialist_info_item_slider,
            R.layout.specialist_experience_item_slider,
            R.layout.specialist_rating_item_slider,
        )

        val titles = listOf("Info", "Experiences", "Reviews")

        val adapter = SpecialistPagerAdapter(layouts, titles)
        binding.viewPager2.adapter = adapter

        // Attach TabLayoutMediator to synchronize TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        // Set the first tab as selected
        binding.tabLayout.getTabAt(1)?.select()

        // Add TabSelectedListener to customize tab background and text colors
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.view?.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.tab_selected_background)

                tab?.view?.let { tabView ->
                    (tabView as ViewGroup).forEach { child ->
                        if (child is TextView) {
                            child.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.white)
                            )
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.view?.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.tab_default_background)

                tab?.view?.let { tabView ->
                    (tabView as ViewGroup).forEach { child ->
                        if (child is TextView) {
                            child.setTextColor(
                                ContextCompat.getColor(requireContext(), R.color.black)
                            )
                        }
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Optional: Define behavior for reselected tabs
            }
        })

        // Chat button click listener
        binding.chat.setOnClickListener {

            if (specialistId != fUser.uid){
                val chatId = createNewChatId()

                // Intent into MessageActivity and sending userId and chatId
                val intent = Intent(requireContext(), MessageActivity::class.java)
                intent.putExtra("receiverId", specialistId)
                intent.putExtra("chatId", chatId)
                startActivity(intent)
            }else{
                Toast.makeText(requireContext(), "You cannot chat with yourself", Toast.LENGTH_SHORT).show()
            }

        }
    }

    // Function to generate a new chatId using Firebase Push ID
    private fun createNewChatId(): String {
        val chatRef = firebaseDatabase.getReference("chats").push() // Create a new chat node
        val chatId = chatRef.key // Firebase will generate a unique key for the chat
        return chatId ?: "defaultChatId" // Return the generated chatId, or a default value if null
    }

    override fun onStart() {
        super.onStart()
        // Set the first tab as selected
        binding.tabLayout.getTabAt(0)?.select()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SpecialistProfileFragment().apply {
                arguments = Bundle().apply { }
            }
    }
}
