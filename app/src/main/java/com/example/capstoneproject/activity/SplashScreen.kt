package com.example.capstoneproject.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.capstoneproject.R


@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen_activity)
        supportActionBar?.hide()

        Handler(Looper.getMainLooper()).postDelayed({
            if(checkNetwork()){
                // Start main activity after the timeout
                val intent = Intent(this@SplashScreen, Authentication::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(this@SplashScreen, Menu1::class.java)
                intent.putExtra("EXTRA_BOOLEAN", false)
                startActivity(intent)
            }
            // Finish splash activity so user cannot go back to it
            finish()
        }, 2000)
    }

    private fun checkNetwork() : Boolean{
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}