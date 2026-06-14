package com.example.neighborhoodemergencyconnect.activities

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.models.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.example.neighborhoodemergencyconnect.databinding.ActivityMainBinding
import com.example.neighborhoodemergencyconnect.fragments.AlertsFragment
import com.example.neighborhoodemergencyconnect.fragments.HomeFragment
import com.example.neighborhoodemergencyconnect.fragments.ProfileFragment
import kotlinx.coroutines.launch
import android.Manifest

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        //Default Fragment
        setContentView(binding.root)
        replaceFragment(HomeFragment())
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1001
            )

        }

        updateFcmToken()

        binding.bottomNavigation.setOnItemSelectedListener {

            when (it.itemId) {

                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.nav_alerts -> {
                    replaceFragment(AlertsFragment())
                    true
                }

                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }
    private fun updateFcmToken() {

        val token = getSharedPreferences(
            "NEC_APP",
            Context.MODE_PRIVATE
        ).getString("token", null)

        if (token == null) return

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { fcmToken ->

                lifecycleScope.launch {

                    try {

                        RetrofitInstance.api.saveFcmToken(
                            token,
                            FcmTokenRequest(fcmToken)
                        )

                        Log.d(
                            "FCM_SAVE",
                            "Token saved successfully"
                        )

                    } catch (e: Exception) {

                        Log.e(
                            "FCM_SAVE",
                            e.message ?: "Unknown error"
                        )
                    }
                }
            }
    }
}