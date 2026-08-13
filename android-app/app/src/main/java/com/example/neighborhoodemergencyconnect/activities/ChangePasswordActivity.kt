package com.example.neighborhoodemergencyconnect.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityChangePasswordBinding
import com.example.neighborhoodemergencyconnect.models.ChangePasswordReq
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.btnSavePassword.setOnClickListener {
            val oldPassword = binding.etOldPassword.text.toString()
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            when {
                oldPassword.isEmpty() -> {
                    binding.tilOldPassword.error = "Please enter your old password"
                }

                newPassword.isEmpty() -> {
                    binding.tilNewPassword.error = "Please enter your new password"
                }

                confirmPassword.isEmpty() -> {
                    binding.tilConfirmPassword.error = "Please enter your confirm password"
                }

                newPassword.length < 6 -> {
                    binding.etNewPassword.error = "Password must be at least 6 characters long"
                }

                newPassword == oldPassword -> {
                    binding.etNewPassword.error = "New password cannot be same as old password"
                }

                confirmPassword != newPassword -> {
                    binding.etConfirmPassword.error = "Passwords do not match"
                }

                else -> {
                    changePassword(oldPassword, newPassword)
                }
            }
        }
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        lifecycleScope.launch {
            binding.btnSavePassword.text = "Please wait..."
            binding.btnSavePassword.isEnabled = false
            try {
                val prefs = getSharedPreferences("NEC_APP", MODE_PRIVATE)
                val token = prefs.getString("token", null)

                if (token.isNullOrEmpty()) {
                    Toast.makeText(this@ChangePasswordActivity, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = RetrofitInstance.api.changePassword(
                    "Bearer $token",
                    ChangePasswordReq(oldPassword, newPassword)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    binding.btnSavePassword.text = "Save Changes"
                    Toast.makeText(
                        this@ChangePasswordActivity,
                        "Password changed successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                } else {
                    Toast.makeText(
                        this@ChangePasswordActivity,
                        "Old password is incorrect",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ChangePasswordActivity,
                    e.message,
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.btnSavePassword.text = "Save Changes"
                binding.btnSavePassword.isEnabled = true
            }
        }
    }
}