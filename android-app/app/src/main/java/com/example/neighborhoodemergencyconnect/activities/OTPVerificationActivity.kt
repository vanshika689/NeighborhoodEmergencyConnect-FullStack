package com.example.neighborhoodemergencyconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityOtpVerificationBinding
import com.example.neighborhoodemergencyconnect.models.OTPVerificationRequest
import kotlinx.coroutines.launch
import org.json.JSONObject

class OTPVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding
    private var email: String = ""
    private var resendCountdown: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get email from intent
        email = intent.getStringExtra("email") ?: ""
        
        if (email.isEmpty()) {
            Toast.makeText(this, "Error: Email not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvEmail.text = "Verification code sent to\n$email"

        // Verify OTP Button
        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()

            if (otp.isEmpty()) {
                binding.etOtp.error = "OTP is required"
                return@setOnClickListener
            }

            if (otp.length != 6) {
                binding.etOtp.error = "OTP must be 6 digits"
                return@setOnClickListener
            }

            verifyOTP(email, otp)
        }

        // Resend OTP Button
        binding.tvResendOtp.setOnClickListener {
            if (resendCountdown <= 0) {
                resendOTP(email)
            } else {
                Toast.makeText(
                    this,
                    "Wait ${resendCountdown}s before resending",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun verifyOTP(email: String, otp: String) {
        binding.btnVerifyOtp.isEnabled = false
        binding.btnVerifyOtp.text = "Verifying..."

        lifecycleScope.launch {
            try {
                val request = OTPVerificationRequest(email = email, otp = otp)
                val response = RetrofitInstance.api.verifyOtp(request)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    
                    // Save JWT token, role, and userId
                    val sharedPreferences = getSharedPreferences("NEC_APP", MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putString("token", body.token)
                        putString("role", body.role)
                        putString("userId", body.userId)
                        putBoolean("isLoggedIn", true)
                        apply()
                    }

                    Toast.makeText(
                        this@OTPVerificationActivity,
                        "Email verified successfully! ✅",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to MainActivity
                    val intent = Intent(this@OTPVerificationActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = try {
                        JSONObject(response.errorBody()?.string() ?: "").getString("message")
                    } catch (e: Exception) {
                        "Verification failed"
                    }
                    Toast.makeText(this@OTPVerificationActivity, errorMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OTPVerificationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            } finally {
                binding.btnVerifyOtp.isEnabled = true
                binding.btnVerifyOtp.text = "Verify OTP"
            }
        }
    }

    private fun resendOTP(email: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.resendOtp(mapOf("email" to email))

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@OTPVerificationActivity,
                        "OTP resent successfully! Check your email",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Start countdown (60 seconds)
                    startResendCountdown()
                } else {
                    val errorMessage = try {
                        JSONObject(response.errorBody()?.string() ?: "").getString("message")
                    } catch (e: Exception) {
                        "Failed to resend OTP"
                    }
                    Toast.makeText(this@OTPVerificationActivity, errorMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@OTPVerificationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun startResendCountdown() {
        resendCountdown = 60
        binding.tvResendOtp.isEnabled = false
        binding.tvResendOtp.text = "Resend OTP (${resendCountdown}s)"

        val timer = Thread {
            while (resendCountdown > 0) {
                Thread.sleep(1000)
                resendCountdown--
                runOnUiThread {
                    if (resendCountdown > 0) {
                        binding.tvResendOtp.text = "Resend OTP (${resendCountdown}s)"
                    } else {
                        binding.tvResendOtp.isEnabled = true
                        binding.tvResendOtp.text = "Resend OTP"
                    }
                }
            }
        }
        timer.start()
    }
}
