package com.example.neighborhoodemergencyconnect.activities
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope

import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityLoginBinding
import com.example.neighborhoodemergencyconnect.models.LoginRequest
import android.util.Log
import com.example.neighborhoodemergencyconnect.models.LoginResponse
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding  = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener {
           val email =  binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
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

            val request = LoginRequest(
                email = email,
                password = password
            )

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.loginUser(request)
                    if(response.isSuccessful){
                        val loginResponse = response.body()
                       val sharedPreferences = getSharedPreferences("NEC_APP", MODE_PRIVATE)
                        sharedPreferences.edit().putString("token", loginResponse?.token).putString("role", loginResponse?.role).apply()

                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Login Successful", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this@LoginActivity, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                    }

                } catch(e: Exception) {
                    Toast.makeText(this@LoginActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }

        }
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)

        }
    }
}