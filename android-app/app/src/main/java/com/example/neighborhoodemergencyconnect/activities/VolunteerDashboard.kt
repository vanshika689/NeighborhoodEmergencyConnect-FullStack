package com.example.neighborhoodemergencyconnect.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityVolunteerDashboardBinding
import kotlinx.coroutines.launch

class VolunteerDashboard : AppCompatActivity() {
    private lateinit var binding : ActivityVolunteerDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityVolunteerDashboardBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        fetchdetails()
    }

    private fun fetchdetails() {
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences("NEC_APP", MODE_PRIVATE).getString("token", null)
                val response = RetrofitInstance.api.getDashboard("$token")
                if (response.isSuccessful) {
                    val dashboardData = response.body()
                    binding.totalAlerts.text = dashboardData?. totalalerts.toString()
                    binding.TotalActive.text = dashboardData?.totalactivealerts.toString()
                    binding.totalResolved.text = dashboardData?.totalresolvedalerts .toString()
                    binding.responded.text = dashboardData?.alertsrespondedbyme.toString()
                    binding.activeResponded.text = dashboardData?.activealertsrespondedbyme.toString()
                    binding.resolved.text = dashboardData?.resolvedalertsrespondedbyme.toString()
                } else {
                    Toast.makeText(this@VolunteerDashboard, "Failed to fetch dashboard data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@VolunteerDashboard, "Error fetching dashboard data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
