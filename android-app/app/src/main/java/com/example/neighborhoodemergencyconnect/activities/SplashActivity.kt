package com.example.neighborhoodemergencyconnect.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.storage.TokenManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize TokenManager from SharedPreferences so interceptor works on cold start
        val prefToken = getSharedPreferences("NEC_APP", MODE_PRIVATE)
            .getString("token", null)
        if (prefToken != null) {
            TokenManager.token = prefToken
        }

        // Show splash for 2 seconds then navigate
        Handler(Looper.getMainLooper()).postDelayed({
            if (prefToken != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000) // 2 seconds delay
    }
}
