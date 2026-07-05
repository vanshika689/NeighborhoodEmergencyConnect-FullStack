package com.example.neighborhoodemergencyconnect.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.neighborhoodemergencyconnect.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val token = getSharedPreferences("NEC_APP", MODE_PRIVATE)
            .getString("token", null)

        // Show splash for 2 seconds then navigate
        Handler(Looper.getMainLooper()).postDelayed({
            if (token != null) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000) // 2 seconds delay
    }
}