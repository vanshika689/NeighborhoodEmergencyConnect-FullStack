package com.example.neighborhoodemergencyconnect.activities

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityAlertsDetailsBinding
import com.example.neighborhoodemergencyconnect.models.Alert
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AlertsDetailsActivity : AppCompatActivity() {
    private var alertId: String?= null
    lateinit var binding: ActivityAlertsDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAlertsDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        alertId = intent.getStringExtra("alertId")
        if (alertId != null) {
            fetchAlertDetails(alertId!!)
        }

        binding.btnRespond.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Respond to Alert")
                .setMessage("Are you sure you want to respond to this alert?")
                .setPositiveButton("Respond") { _, _ ->
                    respondToAlert()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun fetchAlertDetails(alertId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getAlertById(alertId)
                if (response.isSuccessful) {
                    val alert = response.body()?.alert
                    if (alert != null) {
                        displayAlertDetails(alert)
                    } else {
                        Toast.makeText(
                            this@AlertsDetailsActivity,
                            "Failed to fetch alert details",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@AlertsDetailsActivity,
                        "Code: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AlertsDetailsActivity,
                    "Error fetching alert details: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun displayAlertDetails(alert: Alert) {
        binding.tvTitle.text = alert.title
        binding.tvStatus.text = alert.status
        binding.tvDescription.text = alert.description
        binding.tvLocation.text = alert.location
        binding.tvCreatedBy.text = alert.createdBy.name
        binding.tvCreatorEmail.text = alert.createdBy.email
        binding.tvCreatedAt.text = getRelativeTime(alert.createdAt)
        Glide.with(this)
            .load(alert.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(binding.ivAlertImage)
        binding.tvRespondersCount.text = "${alert.responders.size} Responders"
               if (alert.status == "resolved") {
            binding.btnRespond.isEnabled = false
            binding.btnRespond.text = "Alert Resolved"
        }


    }

    fun getRelativeTime(createdAt: String): CharSequence {
        val sdf = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault()
        )
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val time = sdf.parse(createdAt)?.time ?: return createdAt

        return DateUtils.getRelativeTimeSpanString(
            time,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
    }


    private fun respondToAlert() {
        lifecycleScope.launch {
            try {
                val sharedPreferences =
                    getSharedPreferences("NEC_APP", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("token", null)
                val alertId = intent.getStringExtra("alertId")
                if (alertId != null) {
                    val response = RetrofitInstance.api.respondToAlert("$token", alertId)
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@AlertsDetailsActivity,
                            "Alert responded successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@AlertsDetailsActivity,
                            "Code: ${response.code()}\n${response.errorBody()?.string()}",
                            Toast.LENGTH_LONG
                        ).show()

                    }
                }

            } catch (e: Exception) {
                Toast.makeText(
                    this@AlertsDetailsActivity,
                    "Error responding to alert: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }
    }
}