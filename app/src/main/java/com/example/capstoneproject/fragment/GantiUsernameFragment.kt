package com.example.capstoneproject.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.capstoneproject.R
import com.example.capstoneproject.databinding.GantiUsernameFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GantiUsernameFragment : Fragment() {
    private lateinit var binding : GantiUsernameFragmentBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = GantiUsernameFragmentBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.gantiUsernameFragment = this
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

    }

    fun gantiUsername(){
        val usernameBaru = binding.etUsernamebaru.text.toString()
        val updateData = HashMap<String, Any>()
        updateData["username"] = usernameBaru

        buttonAdjust(true)
        val user = auth.currentUser
        database.reference.child(user!!.uid).child("User").updateChildren(updateData)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    Toast.makeText(requireContext(), "Ganti username berhasil", Toast.LENGTH_SHORT).show()
                    buttonAdjust(false)
                }else{
                    Toast.makeText(requireContext(), "${it.exception}", Toast.LENGTH_SHORT).show()
                    binding.tvTitle.text = it.exception.toString()
                    buttonAdjust(false)
                }

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ganti username gagal2", Toast.LENGTH_SHORT).show()
                buttonAdjust(false)
            }
    }

    fun toMenu(){
        findNavController().popBackStack(R.id.menuFragment, false)
    }

    private fun buttonAdjust(isEnableButton : Boolean){
        val progressBar = binding.progressBar
        val btnGanti = binding.btnGanti

        if(isEnableButton){
            btnGanti.apply {
                isEnabled = false
                text = ""
            }
            progressBar.visibility = View.VISIBLE
        }else{
            btnGanti.apply {
                isEnabled = true
                text = getString(R.string.btn_ganti)
            }
            progressBar.visibility = View.GONE
        }
    }
}