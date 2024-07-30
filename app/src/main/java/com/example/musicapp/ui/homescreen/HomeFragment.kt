package com.example.musicapp.ui.homescreen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.musicapp.R
import com.example.musicapp.data.models.MyMusic
import com.example.musicapp.databinding.FragmentHomeBinding
import com.example.musicapp.ui.MainActivity
import com.example.musicapp.ui.MusicViewModel
import com.example.musicapp.ui.homescreen.adapters.MusicAdapter
import com.example.musicapp.ui.homescreen.adapters.MusicClickEvents
import com.example.musicapp.utils.MusicForegroundService
import com.example.musicapp.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), MusicClickEvents {
    private var binding: FragmentHomeBinding? = null

    private val viewModel: MusicViewModel by hiltNavGraphViewModels(R.id.main_navigation)

    private val musicAdapter by lazy {
        MusicAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getMusic()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        (activity as MainActivity).changeStatusBarColor(R.color.royal_blue)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.musicRecyclerView?.adapter = musicAdapter



        lifecycleScope.launch {
            viewModel.musics.collect() { musics ->
                when (musics) {
                    is Resource.Error -> {
                        MainActivity.progress.hide()
                        Toast.makeText(requireContext(), musics.message, Toast.LENGTH_SHORT).show()
                    }



                    is Resource.Loading -> {
                        MainActivity.progress.show()
                    }

                    is Resource.Success -> {
                        MainActivity.progress.hide()
                        musicAdapter.submitList(musics.data)
                    }
                }
            }
        }

    }

    override fun selected(music: MyMusic.Music) {
        viewModel.setSelectedMusic(music)
        findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToMusicPlayerFragment()
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun requestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                Toast.makeText(requireContext(), "permission granted", Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                showDialog(requireContext())
            }

            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    (requireActivity() as MainActivity).requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun showDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setMessage("Notification permission is required ")
            setTitle("Permission required")
            setPositiveButton("Enable") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    (requireActivity() as MainActivity).requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

}