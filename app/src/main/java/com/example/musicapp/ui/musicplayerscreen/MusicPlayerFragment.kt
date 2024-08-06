package com.example.musicapp.ui.musicplayerscreen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.OptIn
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.musicapp.R
import com.example.musicapp.data.models.MyMusic
import com.example.musicapp.data.models.NotificationMusic
import com.example.musicapp.databinding.FragmentMusicPlayerBinding
import com.example.musicapp.ui.MainActivity
import com.example.musicapp.ui.MusicViewModel
import com.example.musicapp.utils.MusicForegroundService
import com.example.musicapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MusicPlayerFragment : Fragment(), MotionLayout.TransitionListener {
    private var binding: FragmentMusicPlayerBinding? = null
    private lateinit var job: Job
    private val TAG = "MusicPlayerFragment"
    private lateinit var player: ExoPlayer
    private val viewModel: MusicViewModel by hiltNavGraphViewModels(R.id.main_navigation)

    private val fragmentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                MusicForegroundService.Actions.NEXT_MUSIC.toString() -> {
                    binding?.nextButton?.performClick()
                }

                MusicForegroundService.Actions.PREVIOUS_MUSIC.toString() -> {
                    binding?.previousButton?.performClick()
                }

                MusicForegroundService.Actions.CHANGE_MUSIC_STATE.toString() -> {
                    binding?.musicButton?.performClick()
                }

            }
        }

    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(requireContext()).build()
        (activity as MainActivity).changeStatusBarColor(R.color.black)

    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(MusicForegroundService.Actions.NEXT_MUSIC.toString())
        intentFilter.addAction(MusicForegroundService.Actions.PREVIOUS_MUSIC.toString())
        intentFilter.addAction(MusicForegroundService.Actions.CHANGE_MUSIC_STATE.toString())
        activity?.registerReceiver(fragmentReceiver, intentFilter)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentMusicPlayerBinding.inflate(inflater, container, false)
        setUpMotionLayoutListener()
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedMusic.collect { music ->
                music?.let {
                    musicSetUp(it)

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bitmap.collect {
                when (it) {
                    is Resource.Error -> {
                        MainActivity.progress.hide()
                    }

                    is Resource.Loading -> {
                        MainActivity.progress.show()
                    }

                    is Resource.Success -> {
                        MainActivity.progress.hide()
                        Intent(
                            requireContext(),
                            MusicForegroundService::class.java
                        ).also { intent ->
                            intent.action = MusicForegroundService.Actions.START.toString()
                            intent.putExtra(
                                "notificationMusic",
                                viewModel.selectedMusic.value?.let { music ->
                                    NotificationMusic(
                                        music = music,
                                        bitmap = it.data,
                                        isPlaying = player.isPlaying,
                                        isFirstMusic = viewModel.checkFirstMusic(),
                                        isLastMusic = viewModel.checkLastMusic()
                                    )
                                }
                            )
                            activity?.startService(intent)
                        }
                    }
                }
            }
        }


        player.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (player.playbackState) {

                    Player.STATE_BUFFERING -> {}

                    Player.STATE_ENDED -> {
                        stopMusic()
                        player.seekTo(0)
                        job.cancel()
                    }

                    Player.STATE_IDLE -> {}

                    Player.STATE_READY -> {
                        job = viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.audioProgress(player).collect {
                                binding?.musicProgress?.progress = it
                            }
                        }
                        binding?.musicProgress?.max = (player.duration / 1000).toInt()
                    }
                }
            }
        })

        binding?.musicProgress?.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    seekBar?.progress = progress
                    seekBar?.progress?.let { player.seekTo((it * 1000).toLong()) }
                    startMusic()
                    job.start()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                job.cancel()
                player.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        binding?.musicButton?.apply {
            setOnClickListener {
                if (player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_ENDED) {
                    if (!player.isPlaying) {
                        startMusic()
                    } else {
                        stopMusic()
                    }
                }
                sendIntent()
            }
        }

        binding?.nextButton?.setOnClickListener {
            viewModel.nextMusic()
            sendIntent()
        }

        binding?.previousButton?.setOnClickListener {
            viewModel.previousMusic()
            sendIntent()
        }

//        binding?.backButton?.setOnClickListener {
////            binding?.rootContainer?.progress = 1.0f
////            binding?.playerContainer?.progress = 1.0f
//            binding?.rootContainer?.apply {
//                transitionToState(R.id.screenEnd,1000)
//            }
////            binding?.playerContainer?.apply {
////                transitionToState(R.id.playerEnd,1000)
////            }
////            binding?.playerContainer?.transitionToEnd()
//        }

    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(fragmentReceiver)
        binding = null
    }

    private fun startMusic() {
        player.play()
        binding?.musicButtonImage?.setImageResource(R.drawable.pause_music)

    }

    private fun stopMusic() {
        player.pause()
        binding?.musicButtonImage?.setImageResource(R.drawable.start_music)

    }

    private fun musicSetUp(music: MyMusic.Music) {
        binding?.backgroundImage?.let {
            Glide.with(requireContext())
                .load(music.album.coverXl)
                .into(
                    it
                )
        }
        binding?.musicImageIv?.let {
            Glide.with(requireContext())
                .load(music.album.coverMedium)
                .into(
                    it
                )
        }
        viewModel.convertToBitmap(requireContext(), music.album.coverMedium)
        binding?.nextButton?.enabled(viewModel.checkLastMusic())
        binding?.previousButton?.enabled(viewModel.checkFirstMusic())
        binding?.musicTitleTv?.text = music.title
        binding?.musicArtistNameTv?.text = music.artist.name
        binding?.musicProgress?.progress = 0
        prepareVoice(music.preview)
    }

    private fun prepareVoice(uri: String) {
        val mediaItem =
            MediaItem.fromUri(uri.toUri())
        player.setMediaItem(mediaItem)
        player.prepare()
    }

    private fun sendIntent() {
        Intent(requireContext(), MusicForegroundService::class.java).also { intent ->
            intent.action = MusicForegroundService.Actions.UPDATE.toString()
            intent.putExtra(
                "notificationMusic",
                viewModel.selectedMusic.value?.let { music ->
                    NotificationMusic(
                        music = music,
                        bitmap = viewModel.bitmap.value.data,
                        isPlaying = player.isPlaying,
                        isFirstMusic = viewModel.checkFirstMusic(),
                        isLastMusic = viewModel.checkLastMusic()
                    )
                }
            )
            activity?.startService(intent)
        }
    }

    private fun ImageView.enabled(flag: Boolean) {
        if (flag) {
            this.isEnabled = false
            this.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.medium_gray
                )
            )
        } else {
            this.isEnabled = true
            this.setColorFilter(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.white
                )
            )
        }
    }

    override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
        updateNestedMotionLayout(motionLayout)
    }

    override fun onTransitionChange(
        motionLayout: MotionLayout?,
        startId: Int,
        endId: Int,
        progress: Float,
    ) {

        updateNestedMotionLayout(motionLayout)
    }

    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
        Log.d(TAG, "onTransitionCompleted: completed")
        if (motionLayout?.id == binding?.rootContainer?.id) {
            binding?.playerContainer?.progress = motionLayout?.progress!!
        }
        updateNestedMotionLayout(motionLayout)
    }

    override fun onTransitionTrigger(
        motionLayout: MotionLayout?,
        triggerId: Int,
        positive: Boolean,
        progress: Float,
    ) {
        updateNestedMotionLayout(motionLayout)
    }

    private fun setUpMotionLayoutListener() = with(binding) {
        this?.rootContainer?.setTransitionListener(this@MusicPlayerFragment)
        this?.playerContainer?.setTransitionListener(this@MusicPlayerFragment)
    }

    private fun updateNestedMotionLayout(motionLayout: MotionLayout?) = motionLayout?.let {
        with(binding) {
            if (it.id == this?.rootContainer?.id) {
//                this.playerContainer.progress = it.progress
                if (it.currentState == R.id.screenEnd) {
                    it.enableTransition(R.id.musicScreenTransition, false)
                } else {
                    it.enableTransition(R.id.musicScreenTransition, true)
                }

            } else if (it.id == this?.playerContainer?.id) {
                this.rootContainer.progress = it.progress
            }
        }
    }


}