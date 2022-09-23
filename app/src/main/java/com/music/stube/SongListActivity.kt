package com.music.stube

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.music.stube.services.OnClearFromRecentService
import java.io.File

class SongListActivity : AppCompatActivity() {

    //TODO Move necessary data to viewModel
    val TAG = "SongListActivity"
    private lateinit var allPaths: ArrayList<String>
    private lateinit var playButton: ImageView
    private lateinit var containerLayout: ConstraintLayout
    private lateinit var nextIcon: ImageView
    private lateinit var prevIcon: ImageView
    private var notiManager: NotificationManager? = null
    private var allTracks = ArrayList<Track>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_list)
        supportActionBar?.hide()

        playButton = findViewById(R.id.startPauseButton)
        containerLayout = findViewById(R.id.constraintLayoutContainer)
        nextIcon = findViewById(R.id.next_icon)
        prevIcon = findViewById(R.id.prev_icon)

        val allSongs = intent.extras?.getStringArrayList("allSongs")
        allPaths = intent.extras?.getStringArrayList("allPaths") as ArrayList<String>

        if (allSongs != null) {
            for (i: Int in allSongs.indices) {
                val track = Track()
                //TODO: Need to change image and artist later
                track.init(allSongs[i], "", R.drawable.download, allPaths[i])
                allTracks.add(track)
            }
        }

        val vm = (application as STubeApplication).appContainer.sharedViewModel
        val mp = (application as STubeApplication).appContainer.mediaPlayer

        vm.allTracks = allTracks

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
            registerReceiver(broadcastReceiver, IntentFilter("TRACKS_TRACKS"))
            startService(Intent(baseContext, OnClearFromRecentService::class.java))
        }

        val adapter = ArrayAdapter(this, R.layout.default_list_view, allSongs!!.toTypedArray())
        val listView = findViewById<View>(R.id.songList) as ListView
        listView.adapter = adapter

        vm.paused.observe(this) {
            if (it == 1) {
                containerLayout.visibility = View.VISIBLE
                playButton.setImageResource(R.drawable.play_icon_playing)
                listView.setPadding(0, 0, 0, 300)
            } else if (it == 2) {
                containerLayout.visibility = View.VISIBLE
                playButton.setImageResource(R.drawable.play_icon_stopped)
                listView.setPadding(0, 0, 0, 300)
            }
        }
        
        listView.onItemClickListener =
            OnItemClickListener { _, _, position, _ -> // Get the selected item text from ListView
                try {
                    Log.i(TAG, "onItemClick")
                    mp.reset()

                    vm.songPosition = position

                    mp.setDataSource(vm.allTracks?.get(vm.songPosition)!!.filePath) //Write your location here
                    mp.prepare()
                    mp.start()
                    vm.paused.value = 1
                    vm.songPosition = position

                    //notify
                    CreateNotification().createNotification(
                        this,
                        vm.allTracks?.get(vm.songPosition)!!,
                        R.drawable.play_icon_playing,
                        vm.songPosition,
                        (allPaths.size - 1),
                        vm,
                        PlaybackStateCompat.STATE_PLAYING
                    )
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        listView.onItemLongClickListener= AdapterView.OnItemLongClickListener { _, _, position, _ ->
            try{
                Log.i(TAG,"onItemLongClickListener -> absolute path: "+allPaths[position]+" file name: "+allSongs[position])
                val uri : Uri = FileProvider.getUriForFile(this,"com.music.stube",File(allPaths[position]))
                Log.i(TAG, "content uri: "+uri)
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    type = "audio/*"
                }

                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            true
        }

        playButton.setOnClickListener {
            playButtonAction()
        }

        nextIcon.setOnClickListener {
            playNextSong()
        }

        prevIcon.setOnClickListener {
            playPrevSong()
        }

        mp.setOnCompletionListener {
            playNextSong()
        }

        val mediaSessionCompat = (application as STubeApplication).mediaSessionCompat
        val seekBarProgressCallBack = object: MediaSessionCompat.Callback() {
            override fun onSeekTo(pos: Long) {
                mp.seekTo(pos.toInt())
            }
        }
        mediaSessionCompat?.setCallback(seekBarProgressCallBack)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notiChannel = NotificationChannel(
                CreateNotification.CHANNEL_ID, "Nur Shuvo Dev",
                NotificationManager.IMPORTANCE_LOW
            )
            notiManager = getSystemService(NotificationManager::class.java)
            if (notiManager != null) {
                notiManager?.createNotificationChannel(notiChannel)
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {

            when (p1?.extras?.get("actionname")) {
                CreateNotification.ACTION_PREVIOUS -> {
                    playPrevSong()
                }
                CreateNotification.ACTION_PLAY -> {
                    playButtonAction()
                }
                else -> {
                    playNextSong()
                }
            }
        }
    }

    private fun playNextSong() {
        val vm = (application as STubeApplication).appContainer.sharedViewModel
        val mp = (application as STubeApplication).appContainer.mediaPlayer
        val nextPosition = (vm.songPosition + 1) % allPaths.size
        try {
            CreateNotification().createNotification(
                this,
                vm.allTracks?.get(vm.songPosition)!!,
                R.drawable.play_icon_playing,
                vm.songPosition,
                (allPaths.size - 1),
                vm,
                PlaybackStateCompat.STATE_STOPPED
            )

            mp.reset()

            vm.songPosition = nextPosition
            mp.setDataSource(vm.allTracks?.get(vm.songPosition)!!.filePath) //Write your location here
            mp.prepare()
            mp.start()
            vm.paused.value = 1
            vm.songPosition = nextPosition

            CreateNotification().createNotification(
                this,
                vm.allTracks?.get(vm.songPosition)!!,
                R.drawable.play_icon_playing,
                vm.songPosition,
                (allPaths.size - 1),
                vm,
                PlaybackStateCompat.STATE_PLAYING
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun playPrevSong() {
        val vm = (application as STubeApplication).appContainer.sharedViewModel
        val mp = (application as STubeApplication).appContainer.mediaPlayer
        val prevPosition = (vm.songPosition - 1 + allPaths.size) % allPaths.size
        try {
            CreateNotification().createNotification(
                this,
                vm.allTracks?.get(vm.songPosition)!!,
                R.drawable.play_icon_playing,
                vm.songPosition,
                (allPaths.size - 1),
                vm,
                PlaybackStateCompat.STATE_STOPPED
            )

            mp.reset()

            vm.songPosition = prevPosition
            mp.setDataSource(vm.allTracks?.get(vm.songPosition)!!.filePath) //Write your location here
            mp.prepare()
            mp.start()
            vm.paused.value = 1
            vm.songPosition = prevPosition

            //notification
            CreateNotification().createNotification(
                this,
                vm.allTracks?.get(vm.songPosition)!!,
                R.drawable.play_icon_playing,
                vm.songPosition,
                (allPaths.size - 1),
                vm,
                PlaybackStateCompat.STATE_PLAYING
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun playButtonAction() {
        Log.d("SongListActivity", "playButtonAction")
        val mp = (application as STubeApplication).appContainer.mediaPlayer
        val vm = (application as STubeApplication).appContainer.sharedViewModel
        if (vm.paused.value == 2) {
            mp.start()
            vm.paused.value = 1

            CreateNotification().createNotification(
                this,
                vm.allTracks?.get(vm.songPosition)!!,
                R.drawable.play_icon_playing,
                vm.songPosition,
                (allPaths.size - 1),
                vm,
                PlaybackStateCompat.STATE_PLAYING
            )

        } else if (vm.paused.value == 1) {
            mp.pause()
            vm.paused.value = 2

            CreateNotification().createNotification(
                this,
                vm.allTracks?.get(vm.songPosition)!!,
                R.drawable.play_icon_stopped,
                vm.songPosition,
                (allPaths.size - 1),
                vm,
                PlaybackStateCompat.STATE_PAUSED
            )
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        notiManager?.cancelAll()
        unregisterReceiver(broadcastReceiver)
    }
}