package com.example.musicapp.data.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class MyMusic(
    @SerializedName("data")
    val `data`: List<Music>,
    @SerializedName("next")
    val next: String,
    @SerializedName("total")
    val total: Int
):Parcelable {
    @Keep
    @Parcelize
    data class Music(
        @SerializedName("album")
        val album: Album,
        @SerializedName("artist")
        val artist: Artist,
        @SerializedName("duration")
        val duration: Int,
        @SerializedName("explicit_content_cover")
        val explicitContentCover: Int,
        @SerializedName("explicit_content_lyrics")
        val explicitContentLyrics: Int,
        @SerializedName("explicit_lyrics")
        val explicitLyrics: Boolean,
        @SerializedName("id")
        val id: Long,
        @SerializedName("link")
        val link: String,
        @SerializedName("md5_image")
        val md5Image: String,
        @SerializedName("preview")
        val preview: String,
        @SerializedName("rank")
        val rank: Int,
        @SerializedName("readable")
        val readable: Boolean,
        @SerializedName("title")
        val title: String,
        @SerializedName("title_short")
        val titleShort: String,
        @SerializedName("title_version")
        val titleVersion: String,
        @SerializedName("type")
        val type: String
    ):Parcelable {
        @Keep
        @Parcelize
        data class Album(
            @SerializedName("cover")
            val cover: String,
            @SerializedName("cover_big")
            val coverBig: String,
            @SerializedName("cover_medium")
            val coverMedium: String,
            @SerializedName("cover_small")
            val coverSmall: String,
            @SerializedName("cover_xl")
            val coverXl: String,
            @SerializedName("id")
            val id: Int,
            @SerializedName("md5_image")
            val md5Image: String,
            @SerializedName("title")
            val title: String,
            @SerializedName("tracklist")
            val tracklist: String,
            @SerializedName("type")
            val type: String
        ):Parcelable

        @Keep
        @Parcelize
        data class Artist(
            @SerializedName("id")
            val id: Int,
            @SerializedName("link")
            val link: String,
            @SerializedName("name")
            val name: String,
            @SerializedName("picture")
            val picture: String,
            @SerializedName("picture_big")
            val pictureBig: String,
            @SerializedName("picture_medium")
            val pictureMedium: String,
            @SerializedName("picture_small")
            val pictureSmall: String,
            @SerializedName("picture_xl")
            val pictureXl: String,
            @SerializedName("tracklist")
            val tracklist: String,
            @SerializedName("type")
            val type: String
        ):Parcelable
    }
}