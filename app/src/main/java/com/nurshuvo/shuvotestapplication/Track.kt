package com.nurshuvo.shuvotestapplication

class Track {
    var title = ""
    var artist = ""
    var image = 0
    var filePath = ""

    fun init(title: String, artist: String, image: Int, filePath: String) {
        this.title = title
        this.artist = artist
        this.image = image
        this.filePath = filePath
    }
}