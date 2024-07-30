package com.example.musicapp.data.repo

import com.example.musicapp.data.api.ApiInterface
import com.example.musicapp.data.models.MyMusic
import retrofit2.Response
import javax.inject.Inject

interface Repository {
    suspend fun getMusic(): Response<MyMusic>
}

class MusicRepo @Inject constructor(private val api: ApiInterface) : Repository {
    override suspend fun getMusic(): Response<MyMusic> {
        return api.getMusic()

    }

}