package com.example.capstoneproject.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.capstoneproject.R
import com.example.capstoneproject.activity.Authentication
import com.example.capstoneproject.activity.KuisActivity
import com.example.capstoneproject.activity.StatistikActivity
import com.example.capstoneproject.databinding.MenuFragmentBinding
import com.example.capstoneproject.util.FragmentViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso


class MenuFragment : Fragment() {
    private lateinit var binding : MenuFragmentBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var actionBarDrawerToggle : ActionBarDrawerToggle
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase
    private lateinit var user : FirebaseUser
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var profilPicture : ImageView
    private lateinit var firebaseStorageRef : StorageReference
    private lateinit var viewModel: FragmentViewModel
    private var isConnectedMenu = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = MenuFragmentBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.menuFragment = this
        isConnectedMenu = requireActivity().intent.getBooleanExtra("EXTRA_BOOLEAN", false)

        if(isConnectedMenu){
            (activity as AppCompatActivity?)!!.supportActionBar!!.show()

            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance()
            user = auth.currentUser!!
            viewModel = ViewModelProvider(this).get(FragmentViewModel::class.java)

            firebaseStorageRef = Firebase.storage.getReference("${user.uid}/user profile.png")
            // Configure Google Sign-In

            updateDrawer()
        }else{
            (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
            changeButtonOffline()
        }
   }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.isDrawerOpen.value = true
        handleButton(false)
        when (item.itemId) {
            android.R.id.home -> {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    viewModel.isDrawerOpen.value = false
                    handleButton(true)
                } else {
                    drawerLayout.openDrawer(GravityCompat.START)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateDrawer(){
        val databaseReference = database.getReference("${user.uid}/User")

        drawerLayout = binding.drawerLayout
        navigationView = binding.navView

        val headerView = navigationView.getHeaderView(0)
        val btnUpdatePicture = headerView.findViewById<ImageView>(R.id.ivGallery)
        val username = headerView.findViewById<TextView>(R.id.tvUsername)
        val email = headerView.findViewById<TextView>(R.id.tvEmail)

        profilPicture = headerView.findViewById(R.id.ivProfilePicture)

        viewModel.isDrawerOpen.observe(viewLifecycleOwner, Observer {
            handleButton(!it)
        })
        
        firebaseStorageRef.downloadUrl
            .addOnSuccessListener {
                Picasso.get().load(it).into(profilPicture)
            }
            .addOnFailureListener {

            }

        actionBarDrawerToggle = ActionBarDrawerToggle(
            requireActivity(), drawerLayout,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // Mengaktifkan action bar
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply {
            title = "Melajah Hanacaraka"
            setDisplayHomeAsUpEnabled(true) // Menampilkan tombol back
            customView = drawerLayout
        }

        setHasOptionsMenu(true)

        navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.changeUsername -> findNavController().navigate(R.id.action_menuFragment_to_gantiUsernameFragment)
                R.id.changePassword -> changePassword()
                R.id.logout -> {
                    googleSignOut()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        btnUpdatePicture.setOnClickListener {
            val intent = Intent()
            intent.setAction(Intent.ACTION_GET_CONTENT)
                .setType("image/*")
            startActivityForResult(intent, 10)
        }

        databaseReference.child("username").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Mendapatkan nilai dari dataSnapshot
                val value = dataSnapshot.getValue(String::class.java)
                // Lakukan sesuatu dengan nilai yang didapatkan
                username.text = value
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle kesalahan jika terjadi
                println("Error getting data : ${databaseError.toException()}")
            }
        })

        databaseReference.child("email").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Mendapatkan nilai dari dataSnapshot
                val value = dataSnapshot.getValue(String::class.java)
                // Lakukan sesuatu dengan nilai yang didapatkan
                email.text = value
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle kesalahan jika terjadi
                println("Error getting data : ${databaseError.toException()}")
            }
        })
    }


    private fun changePassword(){
        user.email?.let {
            auth.sendPasswordResetEmail(it)
                .addOnCompleteListener {
                    Toast.makeText(requireContext(), "Form penggantian password telah dikirim ke email", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun handleButton(isEnabled : Boolean){
        binding.btnKamus.isEnabled = isEnabled
        //binding.btnPelajaran.isEnabled = isEnabled
        binding.btnMenulis.isEnabled = isEnabled
        binding.btnKuis.isEnabled = isEnabled
        binding.btnStatistik.isEnabled = isEnabled
    }

    private fun handleIsNotConnectedMenu(){
        Toast.makeText(requireContext(), "Tidak Ada Koneksi Internet", Toast.LENGTH_SHORT).show()
        Toast.makeText(requireContext(), "Hidupkan koneksi internet, dan buka kembali aplikasi", Toast.LENGTH_SHORT).show()
    }

    private fun googleSignOut(){
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        googleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(requireActivity(), Authentication::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun changeButtonOffline(){
        binding.btnMenulis.setBackgroundResource(R.drawable.btn_menu_round_offline)
        binding.btnStatistik.setBackgroundResource(R.drawable.btn_menu_round_offline)
        binding.btnKuis.setBackgroundResource(R.drawable.btn_menu_round_offline)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 10) {
            data?.let {
                val uri: Uri? = data.data
                profilPicture.setImageURI(uri)
                updateToFirebase(uri)
            }
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun updateToFirebase(uri: Uri?){
        val uploadTask = uri?.let {
            firebaseStorageRef.putFile(it)
        }

        uploadTask?.addOnSuccessListener {
            firebaseStorageRef.downloadUrl.addOnSuccessListener {
                Toast.makeText(requireContext(), "Gambar berhasil diganti", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Gambar gagal diganti", Toast.LENGTH_SHORT).show()
            }
        }?.addOnFailureListener {
            Toast.makeText(requireContext(), "Terdapat error", Toast.LENGTH_SHORT).show()
        }
    }

    fun toKamus(){
        val action = MenuFragmentDirections.actionMenuFragmentToDaftarHanacarakaFragment(true)
        findNavController().navigate(action)
    }

    fun toPelajaran(){
        findNavController().navigate(R.id.action_menuFragment_to_pelajaranFragment)
    }

    fun toStatistik(){
        if(isConnectedMenu){
//            findNavController().navigate(R.id.action_menuFragment_to_statistikFragment)
            val intent = Intent(requireContext(), StatistikActivity::class.java)
            startActivity(intent)
        }else{
            handleIsNotConnectedMenu()
        }
    }

    fun toMenulis(){
        if(isConnectedMenu){
            val action = MenuFragmentDirections.actionMenuFragmentToDaftarHanacarakaFragment(false)
            findNavController().navigate(action)
        }else{
            handleIsNotConnectedMenu()
        }

    }

    fun toKuis(){
        if(isConnectedMenu){
            val intent = Intent(requireActivity(), KuisActivity::class.java)
            startActivity(intent)
        }else{
            handleIsNotConnectedMenu()
        }
    }
}