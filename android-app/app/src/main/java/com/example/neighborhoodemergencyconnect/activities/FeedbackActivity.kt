package com.example.neighborhoodemergencyconnect.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.neighborhoodemergencyconnect.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)


        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSubmitFeedback.setOnClickListener {
            val feedbackText = binding.etFeedbackMessage.text.toString().trim()

            if (feedbackText.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val sharedPreferences = getSharedPreferences("NEC_APP", Context.MODE_PRIVATE)
            val userName = sharedPreferences.getString("userName", "Citizen") ?: "Citizen"
            val userEmail = sharedPreferences.getString("userEmail", "unknown@gmail.com") ?: "unknown@gmail.com"


            val body = "Name: $userName\nEmail: $userEmail\n\nFeedback:\n$feedbackText"
            val recipientEmail = "vanshikaup2705@gmail.com"
            val subject = "CivicGuard App Feedback from $userName"


            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }

            try {
                startActivity(Intent.createChooser(intent, "Send Feedback via..."))
            } catch (e: Exception) {
                Toast.makeText(this, "No email app found on this device", Toast.LENGTH_SHORT).show()
            }
        }
    }
}