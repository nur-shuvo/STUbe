package com.music.stube

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File


class MainActivity : AppCompatActivity() {
        val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.new_layout)
        supportActionBar?.hide()

        if ((SDK_INT >= Build.VERSION_CODES.LOLLIPOP) && !checkPermission()) {
            requestPermission()
        } else {
            doMainTask()
        }
    }

    private fun checkPermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityCompat.requestPermissions(this, arrayOf(READ_EXTERNAL_STORAGE), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (checkPermission()) {
                doMainTask()
            }
        }
    }

    private fun doMainTask() {
        val songList = getPlayList(Environment.getExternalStorageDirectory().path)
        val allSongs = ArrayList<String>()
        val allPaths = ArrayList<String>()
        if (songList != null) {
            for (song in songList) {
                val fileName = song["file_name"]
                val filePath = song["file_path"]
                //here you will get list of file name and file path that present in your device
                println("file details : name = $fileName path = $filePath")
                Log.d(TAG, "doMainTask() -> " + filePath)
                allPaths.add(filePath!!)
                allSongs.add(fileName!!)
            }
        }
        Log.d(TAG, "doMainTask() -> " + (songList == null))
        findViewById<View>(R.id.tv3).setOnClickListener {
            if (songList != null && songList.isEmpty()) {
                Toast.makeText(getApplicationContext(), "songs not found!", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent()
                intent.putExtra("allSongs", allSongs)
                intent.putExtra("allPaths", allPaths)
                intent.action = "com.shuvo.songActivity"
                finish()
                startActivity(intent)
            }
        }
    }

    private fun getPlayList(rootPath: String): ArrayList<HashMap<String, String>>? {
        val fileList: ArrayList<HashMap<String, String>> = ArrayList()
        return try {
            val rootFolder = File(rootPath)
            val files =
                rootFolder.listFiles() //here you will get NPE if directory doesn't contains  any file,handle it like this.
            for (file in files) {
                if (file.isDirectory) {
                    if (getPlayList(file.absolutePath) != null) {
                        fileList.addAll(getPlayList(file.absolutePath)!!)
                    } else {
                        break
                    }
                } else if (file.name.endsWith(".mp3") || file.name.endsWith(".amr") ||
                    file.name.endsWith(".m4a") || file.name.endsWith(".aac"))
                 {
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