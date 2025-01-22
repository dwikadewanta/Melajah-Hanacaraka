package com.example.capstoneproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.capstoneproject.R
import com.example.capstoneproject.adapter.ListHanacarakaAdapter
import com.example.capstoneproject.databinding.DaftarHanacarakaFragmentBinding
import com.example.capstoneproject.util.ListHanacarakaID

class DaftarHanacarakaFragment : Fragment(){
    private lateinit var binding : DaftarHanacarakaFragmentBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListHanacarakaAdapter
    private lateinit var adapterItem : ListHanacarakaID
    private var isKamus = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            isKamus = it.getBoolean("isKamus")
        }
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        val fragmentBinding = DaftarHanacarakaFragmentBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 6)

        adapterItem = ListHanacarakaID()

        val images = adapterItem.images
        val texts = adapterItem.texts

        adapter = ListHanacarakaAdapter(requireContext(), images, texts, isKamus)
        recyclerView.adapter = adapter

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack(R.id.menuFragment, false)
        }
    }
}