package com.music.stube.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

//TODO: Need to investigate why it is needed, or is it really needed?
class OnClearFromRecentService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }
}