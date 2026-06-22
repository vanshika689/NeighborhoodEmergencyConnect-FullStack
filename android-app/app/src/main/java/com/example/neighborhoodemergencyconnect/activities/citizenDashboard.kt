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
import com.example.neighborhoodemergencyconnect.databinding.ActivityCitizenDashboardBinding
import kotlinx.coroutines.launch

class citizenDashboard : AppCompatActivity(){
    private lateinit var binding: ActivityCitizenDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCitizenDashboardBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        fetchdetails()
        binding.btnRaiseAlert.setOnClickListener {
            val intent = Intent(this@citizenDashboard, AddAlert::class.java)
            startActivity(intent)
        }
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()

        }


    }

    private fun fetchdetails() {
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences("NEC_APP", MODE_PRIVATE).getString("token", null)
                val response = RetrofitInstance.api.getDashboard("$token")
                if (response.isSuccessful) {
                    val dashboardData = response.body()
                    binding.tvTotalAlerts.text = dashboardData?. totalalertsbyme.toString()
                    binding.tvActiveCount.text = dashboardData?.totalactivealertsbyme.toString()
                    binding.tvResolvedCount.text = dashboardData?.totalresolvedalerts .toString()
                } else {
                    Toast.makeText(
                        this@citizenDashboard,
                        "Failed to fetch dashboard data",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@citizenDashboard,
                    "Error fetching dashboard data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        }

    }
