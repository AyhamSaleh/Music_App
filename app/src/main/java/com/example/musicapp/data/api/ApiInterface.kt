package com.example.musicapp.data.api

import com.example.musicapp.data.models.MyMusic
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ApiInterface {

    @GET("/search")
    suspend fun getMusic(
        @Header("x-rapidapi-key") key: String = "ef8dd34594msh28f517f4e9e0791p1c05a1jsne673540aa0c5",
        @Query("q") query: String = "TheSynaptik",
    ): Response<MyMusic>
}