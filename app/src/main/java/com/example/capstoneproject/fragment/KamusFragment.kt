package com.example.capstoneproject.fragment

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.capstoneproject.R
import com.example.capstoneproject.databinding.GantiUsernameFragmentBinding
import com.example.capstoneproject.databinding.KamusFragmentBinding
import com.example.capstoneproject.util.ListHanacarakaID

class KamusFragment : Fragment() {
    private lateinit var binding : KamusFragmentBinding
    private lateinit var listID : ListHanacarakaID
    private lateinit var mediaPlayer: MediaPlayer
    private var position = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        arguments?.let {
            position = it.getInt("position")
        }

        val fragmentBinding = KamusFragmentBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.kamusFragment = this
        mediaPlayer = MediaPlayer()

        val ivKamusImage = binding.ivKamusImage
        val tvKamusText = binding.tvKamusText

        listID = ListHanacarakaID()
        val images = listID.images[position]
        val texts = listID.texts[position]

        ivKamusImage.setImageResource(images)
        tvKamusText.text = texts
    }

    fun soundClick(){
        // Release any existing MediaPlayer instance
        mediaPlayer.release()

        // Initialize a new MediaPlayer instance
        mediaPlayer = MediaPlayer.create(requireContext(), listID.sounds[position])

        // Start playing the sound
        mediaPlayer.start()
    }

    fun btnBack(){
        findNavController().popBackStack(R.id.daftarHanacarakaFragment, false)
    }
}