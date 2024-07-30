package com.example.musicapp.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.musicapp.data.models.MyMusic
import com.example.musicapp.data.repo.MusicRepo
import com.example.musicapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MusicViewModel @Inject constructor(private val musicRepo: MusicRepo) : ViewModel() {
    private val TAG = "MusicViewModel"

    private val _musics: MutableStateFlow<Resource<List<MyMusic.Music>>> =
        MutableStateFlow(Resource.Loading())
    val musics: StateFlow<Resource<List<MyMusic.Music>>> = _musics

//    private val _selectedMusic:MutableStateFlow<Resource<MyMusic.Music>> =MutableStateFlow(Resource.Idle())
//    val selectedMusic:StateFlow<Resource<MyMusic.Music>> = _selectedMusic

    private val _selectedMusic: MutableStateFlow<MyMusic.Music?> = MutableStateFlow(null)
    val selectedMusic: StateFlow<MyMusic.Music?> = _selectedMusic

    private val _bitmap: MutableStateFlow<Resource<Bitmap>> =
        MutableStateFlow(Resource.Loading())
    val bitmap: StateFlow<Resource<Bitmap>> = _bitmap

    //    private val _musicProgress: MutableStateFlow<Int> = MutableStateFlow(0)
//    val musicProgress: StateFlow<Int> = _musicProgress

    fun getMusic() {
        viewModelScope.launch {
            _musics.emit(Resource.Loading())
            try {
                val response = musicRepo.getMusic()

                if (response.isSuccessful) {
                    _musics.emit(Resource.Success(response.body()?.data!!))
                } else {
                    _musics.emit((Resource.Error(response.message())))
                }
            } catch (e: Exception) {
                _musics.emit((Resource.Error(e.message ?: "error")))
            }
        }

    }

    fun setSelectedMusic(music: MyMusic.Music) {
        viewModelScope.launch {
            _selectedMusic.emit(music)
        }
    }

//    fun changeMusicProgress(exoPlayer: ExoPlayer) {
//        viewModelScope.launch {
//            _musicProgress.emit(
//                ((exoPlayer.currentPosition.toFloat().div(exoPlayer.duration.toFloat()).times(
//                    exoPlayer.duration / 1000
//                )).toInt())
//            )
//        }
//
//    }


    fun audioProgress(exoPlayer: ExoPlayer) = flow {
        Log.d(TAG, "audioProgress: ${exoPlayer.duration}")
        while (true) {
            emit(
                ((exoPlayer.currentPosition.toFloat().div(exoPlayer.duration.toFloat()).times(
                    exoPlayer.duration / 1000
                )).toInt())
            )
            delay(100)
        }
    }.flowOn(Dispatchers.Main)

    fun nextMusic() {
        viewModelScope.launch {
            val musicList = musics.value.data?.toMutableList()
            var index = musicList?.indexOfFirst { it.id == selectedMusic.value?.id }
            index = index?.plus(1)
            musics.value.data?.get(index!!)?.let { setSelectedMusic(it) }


        }

    }

    fun previousMusic() {
        viewModelScope.launch {
            val musicList = musics.value.data?.toMutableList()
            var index = musicList?.indexOfFirst { it.id == selectedMusic.value?.id }
            index = index?.minus(1)
            musics.value.data?.get(index!!)?.let { setSelectedMusic(it) }
        }
    }

    fun checkFirstMusic(): Boolean {
        val musicList = musics.value.data?.toMutableList()
        val index = musicList?.indexOfFirst { it.id == selectedMusic.value?.id }
        return index == 0
    }

    fun checkLastMusic(): Boolean {
        val musicList = musics.value.data?.toMutableList()
        val index = musicList?.indexOfFirst { it.id == selectedMusic.value?.id }
        return index == musicList?.size?.minus(1)
    }

    fun convertToBitmap(context: Context, uri: String) {

        val target = object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?,
            ) {
                viewModelScope.launch {
                    _bitmap.emit(Resource.Success(resource))
                }

            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                viewModelScope.launch {
                    _bitmap.emit(Resource.Error("error"))
                }
                super.onLoadFailed(errorDrawable)
            }
        }
        try {
            Glide.with(context)
                .asBitmap()
                .load(uri)
                .into(target)
        } catch (e: Exception) {
            viewModelScope.launch {
                _bitmap.emit(Resource.Error(e.toString()))
            }

        }
    }

}