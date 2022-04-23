package com.nurshuvo.shuvotestapplication

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.io.File


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()
        setContentView(R.layout.new_layout)

        val songList = getPlayList(Environment.getExternalStorageDirectory().path)
        val allSongs = ArrayList<String>()
        val allPaths = ArrayList<String>()
        if (songList != null) {
            for (song in songList) {
                val fileName = song["file_name"]
                val filePath = song["file_path"]
                //here you will get list of file name and file path that present in your device
                println("file details : name = $fileName path = $filePath")
                allPaths.add(filePath!!)
                allSongs.add(fileName!!)
            }
        }

        findViewById<View>(R.id.tv3).setOnClickListener {
            val intent = Intent()
            intent.putExtra("allSongs", allSongs)
            intent.putExtra("allPaths", allPaths)
            intent.action = "com.shuvo.songActivity"
            finish()
            startActivity(intent)
        }
    }

    private fun getPlayList(rootPath: String): ArrayList<HashMap<String, String>>? {
        val fileList: ArrayList<HashMap<String, String>> = ArrayList()
        return try {
            val rootFolder = File(rootPath)
            val files = rootFolder.listFiles() //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (file in files) {
                if (file.isDirectory) {
                    if (getPlayList(file.absolutePath) != null) {
                        fileList.addAll(getPlayList(file.absolutePath)!!)
                    } else {
                        break
                    }
                } else if (file.name.endsWith(".mp3") || file.name.endsWith(".amr") ||
                    file.name.endsWith(".m4a")) {
                    val song: HashMap<String, String> = HashMap()
                    song["file_path"] = file.absolutePath
                    song["file_name"] = file.name
                    fileList.add(song)
                }
            }
            fileList
        } catch (e: Exception) {
            null
        }
    }
}