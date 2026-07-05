package com.example.neighborhoodemergencyconnect.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.neighborhoodemergencyconnect.adapters.AlertAdapter
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityMyAlertBinding
import com.example.neighborhoodemergencyconnect.models.Alert
import kotlinx.coroutines.launch

class MyAlert : AppCompatActivity() {

    private lateinit var binding: ActivityMyAlertBinding
    private lateinit var adapter: AlertAdapter
    private val alertList = mutableListOf<Alert>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMyAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }


        binding.btnCreateAlert.setOnClickListener {
            startActivity(Intent(this, AddAlert::class.java))
        }

        adapter = AlertAdapter(alertList) { alert ->
            val intent = Intent(this, AlertsDetailsActivity::class.java)
            intent.putExtra("alertId", alert._id)
            startActivity(intent)
        }

        binding.rvMyAlerts.layoutManager = LinearLayoutManager(this)
        binding.rvMyAlerts.adapter = adapter

        fetchMyAlerts()
    }

    private fun fetchMyAlerts() {

        binding.progressBar.visibility = View.VISIBLE
        binding.layoutEmpty.visibility = View.GONE
        binding.rvMyAlerts.visibility = View.GONE

        lifecycleScope.launch {

            try {

                val token = getSharedPreferences(
                    "NEC_APP",
                    MODE_PRIVATE
                ).getString("token", null)

                if (token == null) {

                    binding.progressBar.visibility = View.GONE

                    Toast.makeText(
                        this@MyAlert,
                        "Token not found",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@launch
                }

                val response = RetrofitInstance.api.myalerts(token)

                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {

                    val alerts = response.body()?.alerts ?: emptyList()

                    alertList.clear()
                    alertList.addAll(alerts)
                    adapter.notifyDataSetChanged()

                    if (alerts.isEmpty()) {

                        binding.layoutEmpty.visibility = View.VISIBLE
                        binding.rvMyAlerts.visibility = View.GONE

                    } else {

                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvMyAlerts.visibility = View.VISIBLE

                    }

                } else {

                    binding.layoutEmpty.visibility = View.VISIBLE

                    Toast.makeText(
                        this@MyAlert,
                        "Failed to fetch alerts",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } catch (e: Exception) {

                binding.progressBar.visibility = View.GONE
                binding.layoutEmpty.visibility = View.VISIBLE

                Toast.makeText(
                    this@MyAlert,
                    e.localizedMessage ?: "Something went wrong",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}