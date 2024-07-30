package com.example.musicapp.ui.homescreen.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.models.MyMusic
import com.example.musicapp.databinding.LayoutMusicBinding
import com.example.musicapp.databinding.LayoutMusicBinding.inflate

class MusicAdapter(private val clickListener: MusicClickEvents) :
    ListAdapter<MyMusic.Music, MusicAdapter.ViewHolder>(MusicDiffCallback()) {
    class ViewHolder private constructor(private val binding: LayoutMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(music: MyMusic.Music,clickListener:MusicClickEvents) {
            binding.musicNameTv.text = music.title
            binding.musicArtistTv.text = music.artist.name
            binding.layout.setOnClickListener {
                clickListener.selected(music)
            }
            Glide.with(binding.root.context).load(music.album.coverXl).centerCrop()
                .placeholder(R.drawable.test_music_cover).into(binding.musicImage)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.bind(item,clickListener)
    }
}

class MusicDiffCallback : DiffUtil.ItemCallback<MyMusic.Music>() {
    override fun areItemsTheSame(oldItem: MyMusic.Music, newItem: MyMusic.Music): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MyMusic.Music, newItem: MyMusic.Music): Boolean {
        return oldItem == newItem
    }


}

interface MusicClickEvents {
    fun selected(music: MyMusic.Music)
}