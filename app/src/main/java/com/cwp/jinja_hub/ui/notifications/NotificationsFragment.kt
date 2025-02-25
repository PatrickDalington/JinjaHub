package com.cwp.jinja_hub.ui.notifications

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cwp.jinja_hub.R
import com.cwp.jinja_hub.adapters.NotificationAdapter
import com.cwp.jinja_hub.alert_dialogs.DeleteDialog
import com.cwp.jinja_hub.databinding.FragmentNotificationsBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotificationsViewModel
    private lateinit var recentAdapter: NotificationAdapter
    private lateinit var earlierAdapter: NotificationAdapter

    // Booleans to track if each list is empty
    private var isRecentEmpty = true
    private var isEarlierEmpty = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]


        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            try {
                MobileAds.initialize(requireActivity()) { status ->
                    Log.d("AdMob", "AdMob Initialized: $status")
                }
            } catch (e: Exception) {
                Log.e("AdMobError", "Failed to initialize AdMob: ${e.message}")
            }
        }


        // Create a new ad view.
        // Find AdView as defined in the layout
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        // Back button.
        binding.back.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Set up recent RecyclerView with its own LayoutManager.
        val recentLayoutManager = LinearLayoutManager(requireContext())
        binding.recentRecycler.layoutManager = recentLayoutManager
        binding.recentRecycler.setHasFixedSize(true)

        // Set up earlier RecyclerView with its own LayoutManager.
        val earlierLayoutManager = LinearLayoutManager(requireContext())
        binding.earlierRecycler.layoutManager = earlierLayoutManager
        binding.earlierRecycler.setHasFixedSize(true)

        // Initialize the adapter for recent notifications.
        recentAdapter = NotificationAdapter { notification, pos ->
            // open DeleteFragment
            val bundle = Bundle()
            bundle.putString("content", notification.content)
            bundle.putString("notificationId", notification.id)
            val dialog = DeleteDialog()
            dialog.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.notifications_container, dialog)
                .addToBackStack(null).commit()


            // Mark Read
            viewModel.markNotificationAsRead(notification.id)
            notification.isRead = true
            recentAdapter.notifyItemChanged(pos)

            //Delete
//            viewModel.deleteNotification(notification.id)
//            recentAdapter.notifyItemRemoved(pos)
        }
        binding.recentRecycler.adapter = recentAdapter

        // Observe recent notifications.
        viewModel.recentNotifications.observe(viewLifecycleOwner) { notifications ->
            if (notifications.isEmpty()) {
                isRecentEmpty = true
                binding.recentRecycler.visibility = View.GONE
            } else {
                isRecentEmpty = false
                binding.recentRecycler.visibility = View.VISIBLE
            }
            recentAdapter.submitList(notifications)
            updateClearAllVisibility()
            updateNotificationImageVisibility()
        }

        // Initialize the adapter for earlier notifications.
        earlierAdapter = NotificationAdapter { notification, pos ->
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("Notification")
                .setMessage(notification.content)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setNegativeButton("Mark as read") { dialog, _ ->
                    dialog.dismiss()
                    viewModel.markNotificationAsRead(notification.id)
                    notification.isRead = true
                    earlierAdapter.notifyItemChanged(pos)
                }
                .setNeutralButton("Delete") { dialog, _ ->
                    dialog.dismiss()
                    viewModel.deleteNotification(notification.id)
                    earlierAdapter.notifyItemRemoved(pos)
                }
                .create()
            dialog.show()
        }
        binding.earlierRecycler.adapter = earlierAdapter

        // Observe earlier notifications.
        viewModel.laterNotifications.observe(viewLifecycleOwner) { notifications ->
            if (notifications.isEmpty()) {
                isEarlierEmpty = true
                binding.earlierRecycler.visibility = View.GONE
            } else {
                isEarlierEmpty = false
                binding.earlierRecycler.visibility = View.VISIBLE
            }
            earlierAdapter.submitList(notifications)
            updateClearAllVisibility()
            updateNotificationImageVisibility()
        }

        // Observe loading state.
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Set up Clear All button listener.
        binding.clearAll.setOnClickListener {
            val dialog = AlertDialog.Builder(requireActivity())
                .setTitle("Notification")
                .setMessage("Are you sure you want to delete all notifications?")
                .setPositiveButton("Yes") { dialog, _ ->
                    viewModel.deleteAllNotifications()
                    recentAdapter.submitList(emptyList())
                    earlierAdapter.submitList(emptyList())
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            dialog.show()
        }
    }

    /**
     * Updates the visibility of the Clear All button based on whether either RecyclerView is visible.
     * If at least one RecyclerView is visible, show Clear All; if both are hidden, hide Clear All.
     */
    private fun updateClearAllVisibility() {
        binding.clearAll.visibility = if (!isRecentEmpty || !isEarlierEmpty) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun updateNotificationImageVisibility() {
        binding.notificationImage.visibility = if (isRecentEmpty) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
