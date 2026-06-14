package com.example.neighborhoodemergencyconnect.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.adapters.AlertAdapter
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityMyAlertBinding
import com.example.neighborhoodemergencyconnect.models.Alert
import kotlinx.coroutines.launch

class MyAlert : AppCompatActivity() {
    private lateinit var binding: ActivityMyAlertBinding
    lateinit var adapter : AlertAdapter
    private  val alertList= mutableListOf<Alert>()

    override fun onCreate(savedInstanceState: Bundle?) {


        binding = ActivityMyAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        adapter = AlertAdapter(alertList){ alert ->
            val intent = Intent(this, AlertsDetailsActivity::class.java)
            intent.putExtra("alertId",alert._id)
            startActivity(intent)
        }
        binding.rvMyAlerts.layoutManager = LinearLayoutManager(this)
        binding.rvMyAlerts.adapter = adapter
        fetchMyAlerts()
    }

    private fun fetchMyAlerts() {

        lifecycleScope.launch {
            try{
                val token = getSharedPreferences(
                    "NEC_APP",
                    MODE_PRIVATE
                ).getString("token", null)
                if (token != null) {
                    val response = RetrofitInstance.api.myalerts("$token")
                    if (response.isSuccessful) {
                        val alerts = response.body()?.alerts
                        alertList.clear()
                        if (alerts != null) {
                            alertList.addAll(alerts)
                            adapter.notifyDataSetChanged()
                        } else {
                            Toast.makeText(this@MyAlert, "No alerts found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MyAlert, "Failed to fetch Alerts", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(this@MyAlert, "Token not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MyAlert, "Error fetching alerts: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        }

    }
}