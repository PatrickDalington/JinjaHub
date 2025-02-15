package com.cwp.jinja_hub.helpers

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.cwp.jinja_hub.model.NotificationModel
import com.cwp.jinja_hub.ui.notifications.NotificationsViewModel

class SendRegularNotification {
    /**
     * Sends a notification by obtaining the NotificationsViewModel from the provided owner,
     * then calling its sendNotification function.
     *
     * @param owner A ViewModelStoreOwner (e.g. an Activity or Fragment) used to obtain the ViewModel.
     * @param notification The NotificationModel to be sent.
     */
    fun sendNotification(id: String, owner: ViewModelStoreOwner, notification: NotificationModel) {
        val notificationViewModel = ViewModelProvider(owner)[NotificationsViewModel::class.java]
        notificationViewModel.sendNotification(id, notification)
    }
}
