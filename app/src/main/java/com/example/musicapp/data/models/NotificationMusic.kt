package com.example.musicapp.data.models

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotificationMusic(
    val music: MyMusic.Music,
    val bitmap:Bitmap?,
    val isPlaying:Boolean?,
    val isFirstMusic:Boolean?,
    val isLastMusic:Boolean?
):Parcelable