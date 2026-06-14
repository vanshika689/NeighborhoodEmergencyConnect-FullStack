package com.example.neighborhoodemergencyconnect.activities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import androidx.lifecycle.lifecycleScope

import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityDashboardBinding
import com.example.neighborhoodemergencyconnect.models.DashboardResponse
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        fetchDashboard()
    }

    private fun fetchDashboard() {
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences("NEC_APP", MODE_PRIVATE).getString("token", null)
                if (token == null) {
                    Toast.makeText(this@DashboardActivity, "Token not found", Toast.LENGTH_SHORT)
                        .show()
                    return@launch
                }
                val response = RetrofitInstance.api.getDashboard("$token")
                if (response.isSuccessful) {
                    response.body()?.let {
                        when (it.role) {
                            "admin" ->
                                setupAdminDashboard(it)

                            "volunteer" ->
                                setupVolunteerDashboard(it)

                            "citizen" ->
                                setupCitizenDashboard(it)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@DashboardActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun setupAdminDashboard(data: DashboardResponse) {
        binding.tvRole.text = "Admin Dashboard"

        binding.tvLabel1.text = "Users"
        binding.tvValue1.text = data.totalusers.toString()

        binding.tvLabel2.text = "Citizens"
        binding.tvValue2.text = data.totalCitizens.toString()

        binding.tvLabel3.text = "Volunteers"
        binding.tvValue3.text = data.totalvolunteers.toString()

        binding.tvLabel4.text = "Total Alerts"
        binding.tvValue4.text = data.totalalerts.toString()

        binding.tvLabel5.text = "Active Alerts"
        binding.tvValue5.text = data.totalactivealerts.toString()
        binding.tvLabel6.text = "Resolved Alerts"
        binding.tvValue6.text = data.totalresolvedalerts.toString()
        binding.tvLabel7.text = "Total Volunteer Requests"
        binding.tvValue7.text = data.totalvolunteerreq.toString()
        binding.tvLabel8.text = "Approved Requests"
        binding.tvValue8.text = data.totalapproved.toString()
        binding.tvLabel9.text = "Pending Requests"
        binding.tvValue9.text = data.totalpending.toString()

    }


    private fun setupVolunteerDashboard(data: DashboardResponse) {
        binding.tvRole.text = "Volunteer Dashboard"

        binding.tvLabel4.text = "Total Responded by me"
        binding.tvValue4.text =
            data.alertsrespondedbyme.toString()

        binding.tvLabel5.text = "Active alerts responded by me"
        binding.tvValue5.text =
            data.activealertsrespondedbyme.toString()

        binding.tvLabel6.text = "Resolved alerts responded by me"
        binding.tvValue6.text =
            data.resolvedalertsrespondedbyme.toString()

        binding.tvLabel1.text = "Total Alerts"
        binding.tvValue1.text =
            data.totalalerts.toString()

        binding.tvLabel2.text = "Total Active"
        binding.tvValue2.text =
            data.totalactivealerts.toString()

        binding.tvLabel3.text = "Total Resolved"
        binding.tvValue3.text = data.totalresolvedalerts.toString()
        binding.card7.visibility = View.GONE
        binding.card8.visibility = View.GONE
        binding.card9.visibility = View.GONE

    }

    private fun setupCitizenDashboard(data: DashboardResponse) {
        binding.tvRole.text = "Citizen Dashboard"
        binding.tvLabel1.text = "My Alerts"
        binding.tvValue1.text = data.totalalertsbyme.toString()

        binding.tvLabel2.text = " Active Alerts by me"
        binding.tvValue2.text = data.totalactivealertsbyme.toString()

        binding.tvLabel3.text = "Resolved Alerts by me"
        binding.tvValue3.text = data.totalresolvedalerts.toString()
        binding.card4.visibility = View.GONE
        binding.card5.visibility = View.GONE
        binding.card6.visibility = View.GONE
        binding.card7.visibility = View.GONE
        binding.card8.visibility = View.GONE
        binding.card9.visibility = View.GONE






    }
}
