package com.bloodbridge.app

import android.app.Application

class BloodBridgeApp : Application() {
    companion object {
        lateinit var instance: BloodBridgeApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
