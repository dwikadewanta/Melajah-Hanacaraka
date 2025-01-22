package com.example.capstoneproject.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.capstoneproject.R
import com.example.capstoneproject.databinding.LupaPasswordFragmentBinding
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LupaPasswordFragment : Fragment() {
    private lateinit var binding : LupaPasswordFragmentBinding
    private lateinit var mAuth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = LupaPasswordFragmentBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lupaPasswordFragment = this
        mAuth = FirebaseAuth.getInstance()
    }

    fun sendEmail(){
        hideKeyboard(binding.etEmail)
        val email = binding.etEmail.text.toString()

        if (email.isEmpty()){
            Toast.makeText(requireContext(), "Isi bagian yang masih kosong", Toast.LENGTH_SHORT).show()
        }else{
            buttonAdjust(true)
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener {
                    if(it.isSuccessful){
                        Toast.makeText(requireContext(), "Form penggantian password telah dikirim ke email", Toast.LENGTH_SHORT).show()
                        buttonAdjust(false)
                    }else{
                        when(it.exception){
                            is FirebaseAuthInvalidCredentialsException -> {
                                Toast.makeText(requireContext(), "Cek kembali format email", Toast.LENGTH_SHORT).show()
                            }
                            is FirebaseTooManyRequestsException -> {
                                Toast.makeText(requireContext(), "Terlalu banyak percobaan, coba lagi nanti", Toast.LENGTH_SHORT).show()
                            }
                            is FirebaseNetworkException -> {
                                Toast.makeText(requireContext(), "Koneksi error", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                Toast.makeText(requireContext(), "Terjadi error, coba lagi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    buttonAdjust(false)
                }
        }
    }

    fun toLoginFragment(){
        findNavController().popBackStack(R.id.loginFragment, false)
    }

    private fun buttonAdjust(isEnableButton : Boolean){
        val progressBar = binding.progressBar
        val btnKirim = binding.btnKirim

        if(isEnableButton){
            btnKirim.apply {
                isEnabled = false
                text = ""
            }
            progressBar.visibility = View.VISIBLE
        }else{
            btnKirim.apply {
                isEnabled = true
                text = getString(R.string.btn_kirim)
            }
            progressBar.visibility = View.GONE
        }
    }

    private fun hideKeyboard(editText1 : EditText) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText1.windowToken, 0)
        editText1.clearFocus()
    }
}