package com.nurshuvo.shuvotestapplication.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionService: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        //Receive media broadcast
        //send broadcast to notify activity by intent filter "TRACKS_TRACKS"
        context?.sendBroadcast(Intent("TRACKS_TRACKS").putExtra("actionname", intent?.action))
    }
}