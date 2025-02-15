package com.cwp.jinja_hub.alert_dialogs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.NotificationAdapter
import com.cwp.jinja_hub.databinding.FragmentDeleteDialogBinding
import com.cwp.jinja_hub.ui.notifications.NotificationsViewModel

class DeleteDialog : Fragment() {

    private lateinit var _binding : FragmentDeleteDialogBinding
    private val binding get() = _binding

    private lateinit var viewModel: NotificationsViewModel
    private lateinit var recentAdapter: NotificationAdapter
    private lateinit var earlierAdapter: NotificationAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDeleteDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get all incoming arguments
        val content = arguments?.getString("content")
        val notificationId = arguments?.getString("notificationId")


        viewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]

        // set content
        binding.content.text = content

        binding.delete.setOnClickListener {
            if (notificationId != null) {
                viewModel.deleteNotification(notificationId)
            }
            parentFragmentManager.popBackStack()
        }

        binding.ok.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DeleteDialog().apply {
                arguments = Bundle().apply {

                }
            }
    }
}