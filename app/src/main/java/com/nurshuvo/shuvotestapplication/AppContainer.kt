package com.nurshuvo.shuvotestapplication

import android.media.MediaPlayer
import android.support.v4.media.session.MediaSessionCompat
import com.nurshuvo.shuvotestapplication.viewmodel.SharedViewModel

class AppContainer {
    var mediaPlayer = MediaPlayer()
    var sharedViewModel = SharedViewModel()
}