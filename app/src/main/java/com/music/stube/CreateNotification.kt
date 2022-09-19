package com.music.stube

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.MediaMetadataRetriever
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.music.stube.services.NotificationActionService
import com.music.stube.viewmodel.SharedViewModel
import java.io.ByteArrayInputStream

/**
 * TODOs
 * 1. Track image data does not need to be extracted here, bitmap conversion is probably costly
 *  move the responsibility  to the callers.
 *  note that createNotification() calls are too frequent
 *
 *  2. Refactor the code (it is too messy)
 */

class CreateNotification() {
    companion object {
        val CHANNEL_ID = "StubeMusicChannel1"

        val ACTION_PREVIOUS = "actionPrevious"
        val ACTION_PLAY = "actionPlay"
        val ACTION_NEXT = "actionNext"
    }

    private lateinit var notification: Notification

    fun createNotification(
        activity: Activity, track: Track, playButton: Int, pos: Int, size: Int,
        vm: SharedViewModel, state: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mp = (activity.application as STubeApplication).appContainer.mediaPlayer
            val mediaSessionCompat = (activity.application as STubeApplication).mediaSessionCompat
            val notiManagerCompat = NotificationManagerCompat.from(activity)

            val mmr = MediaMetadataRetriever()
            mmr.setDataSource(track.filePath)
            val duration =
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
            var inputStream: ByteArrayInputStream? = null
            if (mmr.embeddedPicture != null) {
                inputStream = ByteArrayInputStream(mmr.embeddedPicture)
            }
            mmr.release()

            // Needed to set for android 11.
            mediaSessionCompat?.setMetadata(
                MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                    .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                    .build()
            )

            //TODO Pause in notification is not working properly
            val mPlaybackState = PlaybackStateCompat.Builder()
                .setState(state, mp.currentPosition.toLong(), 1.0f)
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SEEK_TO or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
                .build()
            mediaSessionCompat?.setPlaybackState(mPlaybackState)

            val bitmapIcon = BitmapFactory.decodeStream(inputStream)

            val pendingIntentPrev: PendingIntent?
            var drwPrevious = 0
            val intentPrevious = Intent(activity, NotificationActionService::class.java)
            intentPrevious.action = ACTION_PREVIOUS
            pendingIntentPrev = PendingIntent.getBroadcast(
                activity,
                0,
                intentPrevious,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            drwPrevious = R.drawable.ic_prev

            val pendingIntentPlay: PendingIntent?
            val intentPlay = Intent(activity, NotificationActionService::class.java)
            intentPlay.action = ACTION_PLAY
            pendingIntentPlay = PendingIntent.getBroadcast(
                activity,
                0,
                intentPlay,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val pendingIntentNext: PendingIntent?
            var drwNext = 0
            val intentNext = Intent(activity, NotificationActionService::class.java)
            intentNext.action = ACTION_NEXT
            pendingIntentNext = PendingIntent.getBroadcast(
                activity,
                0,
                intentNext,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
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
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSessionCompat?.sessionToken)
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            notiManagerCompat.notify(1, notification)
        }
    }
}