package com.cwp.jinja_hub

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable offline persistence for Firebase Realtime Database.
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}