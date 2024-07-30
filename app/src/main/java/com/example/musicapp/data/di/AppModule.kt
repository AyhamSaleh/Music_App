package com.example.musicapp.data.di

import com.example.musicapp.data.api.ApiInterface
import com.example.musicapp.data.repo.MusicRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMusicRepo(api: ApiInterface): MusicRepo {
        return MusicRepo(api)
    }
}