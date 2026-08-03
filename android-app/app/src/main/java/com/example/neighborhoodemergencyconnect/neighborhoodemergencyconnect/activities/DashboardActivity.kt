package com.example.neighborhoodemergencyconnect.activities
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance

import com.example.neighborhoodemergencyconnect.databinding.ActivityDashboardBinding
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        fetchDashboardData()
        binding.btnProcessQueue.setOnClickListener {
            val intent = Intent(this@DashboardActivity, VolunteerReq::class.java)
            startActivity(intent)
        }
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchDashboardData() {
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences(
                    "NEC_APP",
                    MODE_PRIVATE
                ).getString("token", null)
                val response = RetrofitInstance.api.getDashboard("$token")
                if (response.isSuccessful) {
                    val dashboardData = response.body()
                    binding.tvTotalUsers.text = dashboardData?.totalusers.toString()
                    binding.tvTotalCitizens.text = dashboardData?.totalCitizens.toString()
                    binding.tvTotalVolunteers.text = dashboardData?.totalvolunteers.toString()
                    binding.tvTotalAlerts.text = dashboardData?.  totalalerts.toString()
                    binding.tvActiveAlerts.text = dashboardData?. totalactivealerts.toString()
                    binding.tvResolvedAlerts.text = dashboardData?. totalresolvedalerts.toString()
                    binding.tvTotalRequests.text = dashboardData?. totalvolunteerreq.toString()
                    binding.tvPending.text = dashboardData?. totalpending.toString()
                    binding.tvApproved.text = dashboardData?.totalapproved.toString()
                    binding.tvRejected.text = dashboardData?.totalrejected.toString()
                } else {
                    Toast.makeText(this@DashboardActivity, "Failed to fetch dashboard data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, "Error fetching dashboard data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}