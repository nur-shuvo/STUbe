package com.music.stube

import android.app.Application
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat

class STubeApplication: Application() {
    val appContainer = AppContainer()
    var mediaSessionCompat: MediaSessionCompat ?= null

    //TODO Check if we can use dependency injection of CreateNotification instead of mediaSessionCompat.
    override fun getApplicationContext(): Context {
        mediaSessionCompat = MediaSessionCompat(super.getApplicationContext(), "tag")
        return super.getApplicationContext()
    }
}