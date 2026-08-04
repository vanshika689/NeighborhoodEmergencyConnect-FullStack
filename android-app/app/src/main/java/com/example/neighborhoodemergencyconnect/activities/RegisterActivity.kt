package com.example.neighborhoodemergencyconnect.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityRegisterBinding
import com.example.neighborhoodemergencyconnect.models.RegisterRequest
import kotlinx.coroutines.launch
import org.json.JSONObject

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignUp.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty()) {
                binding.etName.error = "Name is required"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.etEmail.error = "Email is required"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etEmail.error = "Enter a valid Email address"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Password is required"
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.etPassword.error = "Password must be at least 6 characters long"
                return@setOnClickListener
            }
            if (!binding.cbTerms.isChecked) {
                Toast.makeText(this, "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(name = name, email = email, password = password)

            // Disable button while request in-flight
            binding.btnSignUp.isEnabled = false

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.registerUser(request)

                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!

                        // If server requires OTP, navigate to OTP verification screen
                        if (body.requiresOTP == true || response.code() == 201) {
                            Toast.makeText(this@RegisterActivity, "Registration initiated. Enter OTP sent to your email.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@RegisterActivity, OTPVerificationActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                            finish()
                            return@launch
                        }

                        val sharedPreferences = getSharedPreferences("NEC_APP", MODE_PRIVATE)
                        sharedPreferences.edit().apply {
                            putString("token", body.token)
                            putString("role", body.role)
                            putString("userId", body.userId)
                            apply()
                        }

                        Toast.makeText(this@RegisterActivity, "Registration Successful!", Toast.LENGTH_SHORT).show()
                        // Navigate to Main Activity
                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    } else {
                        val errorMessage = try {
                            JSONObject(response.errorBody()?.string() ?: "").getString("message")
                        } catch (e: Exception) {
                            "Registration failed"
                        }
                        Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.btnSignUp.isEnabled = true
                }
            }
        }
        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
