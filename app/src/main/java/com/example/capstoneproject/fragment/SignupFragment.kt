package com.example.capstoneproject.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.capstoneproject.R
import com.example.capstoneproject.activity.Menu1
import com.example.capstoneproject.databinding.SignupFragmentBinding
import com.example.capstoneproject.util.FirebaseDataClass
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date

class SignupFragment : Fragment() {
    private lateinit var binding: SignupFragmentBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SignupFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        retainInstance = true
        binding.signupFragment = this
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        showPassword()

    }

    fun toLogin(){
        findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
    }

    fun signup(){
        hideKeyboard(binding.etUsername, binding.etEmail, binding.etPassword)
        val username = binding.etUsername.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if(username.isEmpty() || email.isEmpty() || password.isEmpty()){
            Toast.makeText(requireContext(), "Isi bagian yang masih kosong", Toast.LENGTH_SHORT).show()
        }else{
            buttonAdjust(true)
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                if(it.isSuccessful){
                    val user = auth.currentUser
                    saveUserData(user!!.uid, username, email)
                    Toast.makeText(requireContext(), "Register berhasil.", Toast.LENGTH_SHORT).show()
                    buttonAdjust(false)
                    toLogin()
                }else{
                    handleSignupError(it.exception)
                    buttonAdjust(false)
                }
            }
        }
    }

    fun googleSignIn(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        val signInClient = googleSignInClient.signInIntent
        startActivityForResult(signInClient, 9001)
    }

    fun twitterSignIn(){
        val provider = OAuthProvider.newBuilder("twitter.com").addCustomParameter("lang", "en").build()
        auth.startActivityForSignInWithProvider(requireActivity(), provider).addOnSuccessListener {
            checkAndCreateUserData()
            Toast.makeText(requireContext(), "Login berhasil", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireActivity(), Menu1::class.java).putExtra("EXTRA_BOOLEAN", true))
            requireActivity().finish()
        } .addOnFailureListener {
            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
            println("error: ${it.message}")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 9001){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                Toast.makeText(requireContext(), "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun saveUserData(uid: String, username: String, email: String){
        val formattedDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
        database.getReference("$uid/Latihan/$formattedDate").setValue(FirebaseDataClass.Latihan())
        database.getReference("$uid/Latihan/$formattedDate/Tanpa Pengangge").setValue(FirebaseDataClass.LatihanTanpaPengangge())
        database.getReference("$uid/Latihan/$formattedDate/Pengangge Ulu").setValue(FirebaseDataClass.LatihanPenganggeUlu())
        database.getReference("$uid/Latihan/$formattedDate/Pengangge Suku").setValue(FirebaseDataClass.LatihanPenganggeSuku())
        database.getReference("$uid/Latihan/$formattedDate/Pengangge Taleng").setValue(FirebaseDataClass.LatihanPenganggeTaleng())
        database.getReference("$uid/Latihan/$formattedDate/Pengangge Taleng Tedong").setValue(FirebaseDataClass.LatihanPenganggeTalengTedong())
        database.getReference("$uid/Latihan/$formattedDate/Pengangge Pepet").setValue(FirebaseDataClass.LatihanPenganggePepet())
        database.getReference("$uid/Kuis/$formattedDate").setValue(FirebaseDataClass.Kuis())
        database.getReference("$uid/User").setValue(FirebaseDataClass.User(username, email))
    }

    private fun handleSignupError(e: Exception?){
        when(e){
            is FirebaseAuthWeakPasswordException -> {
                Toast.makeText(requireContext(), "Isi password setidaknya 6 karakter", Toast.LENGTH_SHORT).show()
            }
            is FirebaseAuthUserCollisionException -> {
                Toast.makeText(requireContext(), "Email sudah terdaftar", Toast.LENGTH_SHORT).show()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                Toast.makeText(requireContext(), "Format email invalid", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(requireContext(), "Terjadi error, coba lagi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            Toast.makeText(requireContext(), "Login berhasil", Toast.LENGTH_SHORT).show()
            checkAndCreateUserData()
            startActivity(Intent(requireActivity(), Menu1::class.java).putExtra("EXTRA_BOOLEAN", true))
            requireActivity().finish()
        }
    }

    private fun checkAndCreateUserData(){
        val user = auth.currentUser
        var isCreateFieldData = true
        database.reference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(userSnapshot in snapshot.children){
                    if(userSnapshot.key.equals(user!!.uid)){
                        isCreateFieldData = false
                        break
                    }
                }
                if(isCreateFieldData){
                    saveUserData(user!!.uid, user.email.toString(), user.email.toString())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Database error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun buttonAdjust(isEnableButton : Boolean){
        binding.apply {
            btnRegister.isEnabled = !isEnableButton
            btnRegister.text = if (isEnableButton) "" else getString(R.string.btn_register)
            progressBar.visibility = if (isEnableButton) View.VISIBLE else View.GONE
        }
    }

    private fun hideKeyboard(vararg editTexts: EditText) {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        editTexts.forEach { editText ->
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
            editText.clearFocus()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun showPassword(){
        binding.etPassword.setOnTouchListener { v, event ->
            val drawableEnd = binding.etPassword.compoundDrawablesRelative[2] // Index 2 is for end drawable
            if (event.action == MotionEvent.ACTION_UP && event.rawX >= (binding.etPassword.right - drawableEnd.bounds.width())) {
                if(binding.etPassword.inputType == 129){
                    binding.etPassword.inputType = 1
                }else{
                    binding.etPassword.inputType = 129
                }
                true
            } else {
                false // Let the EditText handle the event
            }
        }
    }
}