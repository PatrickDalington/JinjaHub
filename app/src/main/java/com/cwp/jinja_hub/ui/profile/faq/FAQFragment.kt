package com.cwp.jinja_hub.ui.profile.faq

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.FAQAdapter
import com.cwp.jinja_hub.databinding.FragmentFAQBinding
import com.cwp.jinja_hub.model.FAQCardItem
import com.cwp.jinja_hub.ui.profile.UserProfileViewModel
import kotlin.concurrent.fixedRateTimer


class FAQFragment : Fragment() {
    private lateinit var _binding: FragmentFAQBinding
    private val binding get() = _binding

    private lateinit var headerView: View
    private lateinit var title: TextView
    private lateinit var backButton: ImageView
    private lateinit var editProfile: ImageView

    private val viewModel: UserProfileViewModel by viewModels()

    private var fullFaqList: List<FAQCardItem> = emptyList()
    private var isShowingAllFaq = false


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
        _binding = FragmentFAQBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        // Getting views from generic layout holder (title, back button)
        // Get the parent fragment's view (which should include the header)

        headerView = requireActivity().findViewById(R.id.prof_header)
        title = headerView.findViewById(R.id.title)
        backButton = headerView.findViewById(R.id.backButton)
        editProfile = headerView.findViewById(R.id.edit_profile)

        // Set title
        title.text = "FAQ"


        // Back button
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Hide edit profile button for this fragment
        editProfile.visibility = View.GONE


        // setup recyclerview and pass adapter
        val linearLayoutManager = LinearLayoutManager(requireActivity())
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.faqRecycler.layoutManager = linearLayoutManager

        // pass adapter to recyclerview
        val adapter = FAQAdapter(emptyList())
        binding.faqRecycler.adapter = adapter


        viewModel.faq.observe(viewLifecycleOwner) { faqList ->
            fullFaqList = faqList

            if (!isShowingAllFaq && faqList.isNotEmpty() && faqList.size > 4) {
                // Load only the first 4 items initially
                adapter.updateFAQList(faqList.take(4))
                binding.viewAllArticles.text = "See more ${faqList.size - 4} articles"
            } else {
                // If already showing all items, update with full list
                adapter.updateFAQList(faqList)
                binding.viewAllArticles.visibility = View.GONE

            }

        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.faqRecycler.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.faqRecycler.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }


        binding.viewAllArticles.setOnClickListener{
            // show all the article in the adapter
            isShowingAllFaq = true

            adapter.updateFAQList(fullFaqList)
            binding.viewAllArticles.visibility = View.GONE
        }


    }


    class LastItemBottomMarginDecoration(private val bottomMarginDp: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: android.graphics.Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            if (position == state.itemCount - 1) {
                val bottomMarginPx = (bottomMarginDp * view.context.resources.displayMetrics.density).toInt()
                outRect.bottom = bottomMarginPx
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FAQFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}