package com.example.musicapp.utils


import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.musicapp.R
import com.example.musicapp.data.models.NotificationMusic
import com.example.musicapp.utils.Constants.CHANNEL_ID
import com.example.musicapp.utils.Constants.NOTIFY_ID
import com.example.musicapp.utils.extinctions.getParcelableCompat


class MusicForegroundService : Service() {

    private var isPlaying: Boolean = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> start(intent)
            Actions.STOP.toString() -> stopSelf()
            Actions.UPDATE.toString() -> updateNotification(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun start(intent: Intent?) {
        val notificationMusic = intent?.getParcelableCompat<NotificationMusic>("notificationMusic")
        isPlaying = notificationMusic?.isPlaying ?: true
        startForeground(1, notificationMusic?.let { showNotification(it) })
    }

    @SuppressLint("MissingPermission")
    fun showNotification(notificationMusic: NotificationMusic): Notification {
        val notificationView = RemoteViews(this.packageName, R.layout.layout_notification)
        notificationView.setTextViewText(R.id.musicNameTv, notificationMusic.music.title)
        notificationView.setTextViewText(R.id.artistNameTv, notificationMusic.music.artist.name)
        notificationView.setImageViewBitmap(R.id.image, notificationMusic.bitmap)

        if (isPlaying) {
            notificationView.setImageViewResource(R.id.playButtonIv, R.drawable.pause_music)
        } else {
            notificationView.setImageViewResource(R.id.playButtonIv, R.drawable.start_music)
        }

        if (!notificationMusic.isLastMusic!!) {
            notificationView.setOnClickPendingIntent(
                R.id.nextButtonIv,
                getIntent(Actions.NEXT_MUSIC.toString())
            )
            notificationView.setInt(
                R.id.nextButtonIv,
                "setColorFilter",
                Color.parseColor("#FFFFFFFF")
            )
        } else {
            notificationView.setInt(
                R.id.nextButtonIv,
                "setColorFilter",
                Color.parseColor("#888888")
            )
        }
        if (!notificationMusic.isFirstMusic!!) {
            notificationView.setOnClickPendingIntent(
                R.id.previousButtonIv,
                getIntent(Actions.PREVIOUS_MUSIC.toString())
            )
            notificationView.setInt(
                R.id.previousButtonIv,
                "setColorFilter",
                Color.parseColor("#FFFFFFFF")
            )
        } else {
            notificationView.setInt(
                R.id.previousButtonIv,
                "setColorFilter",
                Color.parseColor("#888888")
            )
        }

        notificationView.setOnClickPendingIntent(
            R.id.playButtonIv,
            getIntent(Actions.CHANGE_MUSIC_STATE.toString())
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationView)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(intent: Intent?) {
        val notificationMusic = intent?.getParcelableCompat<NotificationMusic>("notificationMusic")
        isPlaying =
            notificationMusic?.isPlaying ?: true
        val notification = notificationMusic?.let { showNotification(it) }
        val notificationManagerCompat = (NotificationManagerCompat.from(this))
        notification?.let { notificationManagerCompat.notify(NOTIFY_ID, it) }
    }

    enum class Actions {
        START, STOP, UPDATE, CHANGE_MUSIC_STATE, NEXT_MUSIC, PREVIOUS_MUSIC
    }

    private fun getIntent(action: String): PendingIntent {
        val intent = Intent(action)
        return  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}