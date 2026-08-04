package com.example.neighborhoodemergencyconnect.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityAlertsDetailsBinding
import com.example.neighborhoodemergencyconnect.models.Alert
import com.example.neighborhoodemergencyconnect.models.ResolveRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AlertsDetailsActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST = 1001
    private var alertId: String? = null
    lateinit var binding: ActivityAlertsDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertsDetailsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        alertId = intent.getStringExtra("alertId")

        alertId?.let { fetchAlertDetails(it) }

        binding.btnRespond.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Respond to Alert")
                .setMessage("Are you sure you want to respond to this alert?")
                .setPositiveButton("Respond") { _, _ -> respondToAlert() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun getFormattedAuthHeader(): String? {
        val sharedPreferences = getSharedPreferences("NEC_APP", Context.MODE_PRIVATE)
        val rawToken = sharedPreferences.getString("token", null)

        if (rawToken.isNullOrEmpty()) return null

        return if (rawToken.startsWith("Bearer ")) rawToken else "Bearer $rawToken"
    }

    private fun fetchAlertDetails(alertId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getAlertById(alertId)
                if (response.isSuccessful) {
                    response.body()?.alert?.let { displayAlertDetails(it) } ?: run {
                        Toast.makeText(this@AlertsDetailsActivity, "Failed to fetch alert details", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@AlertsDetailsActivity, "Code: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@AlertsDetailsActivity, "${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayAlertDetails(alert: Alert) {
        val sharedPreferences = getSharedPreferences("NEC_APP", Context.MODE_PRIVATE)
        val userRole = sharedPreferences.getString("role", "citizen") ?: "citizen"
        val userId = sharedPreferences.getString("userId", "") ?: ""

        val isResolved = alert.status.equals("resolved", ignoreCase = true)
        val hasResponded = alert.responders.any { it._id == userId }

        if (userRole == "volunteer" || userRole == "admin") {
            binding.btnRespond.visibility = View.VISIBLE
            binding.btnResolve.visibility = View.VISIBLE
        } else {
            binding.btnRespond.visibility = View.GONE
            binding.btnResolve.visibility = View.GONE
        }

        if (isResolved) {
            binding.btnRespond.isEnabled = false
            binding.btnRespond.text = "Alert Resolved"
        } else if (hasResponded) {
            binding.btnRespond.text = "Responded Successfully"
            binding.btnRespond.isEnabled = false
        } else {
            binding.btnRespond.text = "Respond"
            binding.btnRespond.isEnabled = true
        }

        if (isResolved) {
            binding.btnResolve.isEnabled = false
            binding.btnResolve.text = "Already Resolved"
        } else {
            binding.btnResolve.isEnabled = hasResponded
            binding.btnResolve.text = "Resolve"

            binding.btnResolve.setOnClickListener {
                if (!hasResponded) {
                    Toast.makeText(this, "Please respond to the alert first.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                MaterialAlertDialogBuilder(this)
                    .setTitle("Resolve Alert")
                    .setMessage("Are you at the alert location? Your GPS will be verified.")
                    .setPositiveButton("Yes, Resolve") { _, _ -> checkLocationAndResolve() }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        if (isResolved && alert.resolvedBy != null) {
            binding.tvResolvedByLabel.visibility = View.VISIBLE
            binding.resolvedByLayout.visibility = View.VISIBLE
            binding.tvResolvedByName.text = alert.resolvedBy.name
            binding.tvResolvedByEmail.text = alert.resolvedBy.email
            binding.tvResolvedById.text = "ID: ${alert.resolvedBy._id}"
        } else {
            binding.tvResolvedByLabel.visibility = View.GONE
            binding.resolvedByLayout.visibility = View.GONE
        }

        binding.tvTitle.text = alert.title
        binding.tvStatus.text = alert.status.uppercase()
        binding.tvStatus.setBackgroundResource(
            if (isResolved) R.drawable.bg_status_resolved else R.drawable.bg_status_active
        )

        binding.tvDescription.text = alert.description
        binding.tvLocation.text = alert.shortAddress
        binding.tvCreatedBy.text = alert.createdBy.name
        binding.tvCreatorEmail.text = alert.createdBy.email
        binding.tvCreatedAt.text = getRelativeTime(alert.createdAt)

        binding.tvFakeReason.text = alert.aiAnalysis?.fakeReason ?: "No analysis available"
        binding.tvConfidence.text = alert.aiAnalysis?.confidence ?: "N/A"
        binding.tvSeverityReason.text = alert.aiAnalysis?.severityReason ?: "N/A"
        binding.tvSeverity.text = alert.aiAnalysis?.severity

        when (alert.aiAnalysis?.severity?.uppercase()) {
            "LOW" -> binding.tvSeverity.setBackgroundResource(R.drawable.bg_pill_low)
            "MEDIUM" -> binding.tvSeverity.setBackgroundResource(R.drawable.bg_pill_medium)
            "HIGH" -> binding.tvSeverity.setBackgroundResource(R.drawable.bg_pill_high)
            "CRITICAL" -> binding.tvSeverity.setBackgroundResource(R.drawable.bg_pill_red)
        }

        if (alert.aiAnalysis?.isFake == true) {
            binding.tvFakeStatus.text = "✕ FAKE"
            binding.tvFakeStatus.setBackgroundResource(R.drawable.bg_pill_fake)
            binding.tvSeverity.visibility = View.GONE
        } else {
            binding.tvFakeStatus.text = "✓ REAL"
            binding.tvFakeStatus.setBackgroundResource(R.drawable.bg_pill_green)
            binding.tvSeverity.visibility = View.VISIBLE
        }

        binding.tvImageLoading.visibility = View.VISIBLE
        Glide.with(this)
            .load(alert.imageUrl)
            .placeholder(android.R.color.darker_gray)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean
                ): Boolean {
                    binding.tvImageLoading.text = "❌ Image not available"
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable, model: Any, target: Target<Drawable>,
                    dataSource: DataSource, isFirstResource: Boolean
                ): Boolean {
                    binding.tvImageLoading.visibility = View.GONE
                    return false
                }
            })
            .into(binding.ivAlertImage)

        binding.tvRespondersCount.text = "${alert.responders.size} Responders"
    }

    private fun getRelativeTime(createdAt: String): CharSequence {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val time = sdf.parse(createdAt)?.time ?: return createdAt
            DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
        } catch (e: Exception) {
            createdAt
        }
    }

    private fun respondToAlert() {
        val currentAlertId = alertId ?: return

        lifecycleScope.launch {
            try {
                val bearerToken = getFormattedAuthHeader()

                if (bearerToken.isNullOrEmpty()) {
                    Toast.makeText(this@AlertsDetailsActivity, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val response = RetrofitInstance.api.respondToAlert(bearerToken, currentAlertId)

                if (response.isSuccessful) {
                    Toast.makeText(this@AlertsDetailsActivity, "Alert responded successfully", Toast.LENGTH_SHORT).show()
                    fetchAlertDetails(currentAlertId)
                } else {
                    val errorMessage = try {
                        JSONObject(response.errorBody()?.string() ?: "").getString("message")
                    } catch (e: Exception) {
                        "Something went wrong"
                    }
                    Toast.makeText(this@AlertsDetailsActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AlertsDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkLocationAndResolve() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                resolveAlert(location.latitude, location.longitude)
            } else {
                Toast.makeText(this, "Unable to get location. Please enable GPS.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resolveAlert(lat: Double, lng: Double) {
        val currentAlertId = alertId ?: return
        lifecycleScope.launch {
            try {
                val bearerToken = getFormattedAuthHeader()

                if (bearerToken.isNullOrEmpty()) {
                    Toast.makeText(this@AlertsDetailsActivity, "Session expired. Please log in again.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val request = ResolveRequest(status = "resolved", volunteerLat = lat, volunteerLng = lng)
                val response = RetrofitInstance.api.updateAlertStatus(bearerToken, currentAlertId, request)

                if (response.isSuccessful) {
                    Toast.makeText(this@AlertsDetailsActivity, "✅ Alert Resolved Successfully!", Toast.LENGTH_SHORT).show()
                    fetchAlertDetails(currentAlertId)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@AlertsDetailsActivity, "Error: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AlertsDetailsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationAndResolve()
        } else {
            Toast.makeText(this, "Location permission required to resolve alert", Toast.LENGTH_SHORT).show()
        }
    }
}