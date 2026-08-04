package com.example.neighborhoodemergencyconnect.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityOtpVerificationBinding
import com.example.neighborhoodemergencyconnect.models.OTPVerificationRequest
import com.example.neighborhoodemergencyconnect.storage.TokenManager
import kotlinx.coroutines.launch
import org.json.JSONObject

class OTPVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding
    private var email: String = ""
    private var resendCountdownSeconds: Long = 0
    private var countdownTimer: CountDownTimer? = null

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

        // Real-time validation: enable verify only when OTP length is 6
        binding.etOtp.doAfterTextChanged {
            val otp = it?.toString()?.trim() ?: ""
            binding.btnVerifyOtp.isEnabled = otp.length == 6
        }
        // Ensure initial enabled state
        binding.btnVerifyOtp.isEnabled = false

        // Verify OTP Button
        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            verifyOTP(email, otp)
        }

        // Resend OTP Button
        binding.tvResendOtp.setOnClickListener {
            if (resendCountdownSeconds <= 0) {
                resendOTP(email)
            } else {
                Toast.makeText(
                    this,
                    "Wait ${resendCountdownSeconds}s before resending",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun verifyOTP(email: String, otp: String) {
        binding.btnVerifyOtp.isEnabled = false
        val originalText = binding.btnVerifyOtp.text
        binding.btnVerifyOtp.text = "Verifying..."

        lifecycleScope.launch {
            try {
                val request = OTPVerificationRequest(email = email, otp = otp)
                val response = RetrofitInstance.api.verifyOtp(request)

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    // Save JWT token (prefix with Bearer) and other details
                    val bearerToken = "Bearer ${body.token}"
                    val sharedPreferences = getSharedPreferences("NEC_APP", MODE_PRIVATE)
                    sharedPreferences.edit().apply {
                        putString("token", bearerToken)
                        putString("role", body.role)
                        putString("userId", body.userId)
                        putBoolean("isLoggedIn", true)
                        apply()
                    }
                    // Also set TokenManager for interceptor usage
                    TokenManager.token = bearerToken

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
                binding.btnVerifyOtp.text = originalText
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
                    startResendCountdown(60_000L)
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

    private fun startResendCountdown(durationMs: Long) {
        countdownTimer?.cancel()
        resendCountdownSeconds = durationMs / 1000
        binding.tvResendOtp.isEnabled = false

        countdownTimer = object : CountDownTimer(durationMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendCountdownSeconds = millisUntilFinished / 1000
                binding.tvResendOtp.text = "Resend OTP (${resendCountdownSeconds}s)"
            }
            
            override fun onFinish() {
                resendCountdownSeconds = 0
                binding.tvResendOtp.isEnabled = true
                binding.tvResendOtp.text = "Resend OTP"
            }
        }
        countdownTimer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}
