package com.nurshuvo.shuvotestapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nurshuvo.shuvotestapplication.Track

class SharedViewModel : ViewModel() {
    var paused = MutableLiveData<Int>(0)
    var songPosition = 0
    var allTracks: ArrayList<Track>? = null
}