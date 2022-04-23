package com.nurshuvo.shuvotestapplication

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nurshuvo.shuvotestapplication.services.NotificationActionService

class CreateNotification {
    companion object {
        public val CHANNEL_ID = "StubeMusicChannel1"

        public val ACTION_PREVIOUS = "actionPrevious"
        public val ACTION_PLAY = "actionPlay"
        public val ACTION_NEXT = "actionNext"
    }

    private lateinit var notification: Notification

    fun createNotification(activity: Activity, track: Track, playButton: Int, pos: Int, size: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notiManagerCompat = NotificationManagerCompat.from(activity)
            val mediaSessionCompat = MediaSessionCompat(activity, "tag")

            val mp = (activity.application as STubeApplication).appContainer.mediaPlayer

            // Needed to set for android 11.
            mediaSessionCompat.setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                    .build()
            )

            val bitmapIcon = BitmapFactory.decodeResource(activity.resources, track.image)

            var pendingIntentPrev: PendingIntent ?= null
            var drwPrevious = 0
            val intentPrevious = Intent(activity, NotificationActionService::class.java)
            intentPrevious.action = ACTION_PREVIOUS
            pendingIntentPrev = PendingIntent.getBroadcast(activity, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT)
            drwPrevious =  R.drawable.ic_prev

            var pendingIntentPlay: PendingIntent ?= null
            val intentPlay = Intent(activity, NotificationActionService::class.java)
            intentPlay.action = ACTION_PLAY
            pendingIntentPlay = PendingIntent.getBroadcast(activity, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT)

            var pendingIntentNext: PendingIntent ?= null
            var drwNext = 0
            val intentNext= Intent(activity, NotificationActionService::class.java)
            intentNext.action = ACTION_NEXT
            pendingIntentNext = PendingIntent.getBroadcast(activity, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT)
            drwNext = R.drawable.ic_next

            //create notification
            notification = NotificationCompat.Builder(activity, CHANNEL_ID)
                .setSmallIcon(R.drawable.play_icon_stopped)
                .setContentTitle(track.title)
                .setContentText(track.artist)
                .setLargeIcon(bitmapIcon)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .addAction(drwPrevious, "Previous", pendingIntentPrev)
                .addAction(playButton, "Play", pendingIntentPlay)
                .addAction(drwNext, "Next", pendingIntentNext)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0,1,2)
                    .setMediaSession(mediaSessionCompat.sessionToken))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            notiManagerCompat.notify(1, notification)
        }
    }
}