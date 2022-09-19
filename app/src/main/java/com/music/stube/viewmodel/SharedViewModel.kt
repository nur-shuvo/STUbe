package com.music.stube.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.music.stube.Track

class SharedViewModel : ViewModel() {
    var paused = MutableLiveData<Int>(0)
    var songPosition = 0
    var allTracks: ArrayList<Track>? = null
}