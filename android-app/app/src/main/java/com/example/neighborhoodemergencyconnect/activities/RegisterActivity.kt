package com.example.neighborhoodemergencyconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityRegisterBinding
import com.example.neighborhoodemergencyconnect.models.RegisterRequest
import kotlinx.coroutines.launch

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

            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.etEmail.error = "Enter a valid Email address"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Password is required"
                return@setOnClickListener
            }

            if(password.length < 6){
                binding.etPassword.error = "Password must be at least 6 characters long"
                return@setOnClickListener
            }

            if(!binding.cbTerms.isChecked){
                Toast.makeText(this, "Please accept the Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(
                name = name,
                email = email,
                password = password
            )


            lifecycleScope.launch { ////so that UI dont get freeze

                try {
                    val response = RetrofitInstance.api.registerUser(request)
                    val sharedPreferences = getSharedPreferences("NEC_APP", MODE_PRIVATE)
                    sharedPreferences.edit().putString("token", response.body()?.token).putString("role", response.body()?.role).apply()
                    if (response.isSuccessful) {
                        val registerResponse = response.body()
                        Toast.makeText(
                            this@RegisterActivity,
                            registerResponse?.message ?: "Registration Successful",
                            Toast.LENGTH_SHORT
                        ).show()

                        binding.etName.text?.clear()
                        binding.etEmail.text?.clear()
                        binding.etPassword.text?.clear()
                        binding.cbTerms.isChecked = false



                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration Failed",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        this@RegisterActivity,
                        e.message,
                        Toast.LENGTH_LONG
                    ).show()

                }

            }
        }

        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}




