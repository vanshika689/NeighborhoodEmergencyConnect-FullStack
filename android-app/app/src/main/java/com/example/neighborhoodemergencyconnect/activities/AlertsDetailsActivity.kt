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
import android.Manifest
import android.util.Log
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.neighborhoodemergencyconnect.models.ResolveRequest
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityAlertsDetailsBinding
import com.example.neighborhoodemergencyconnect.models.Alert

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
        binding = ActivityAlertsDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
                Log.d("API_RESPONSE", response.errorBody()?.string() ?: "No error")
                Log.d("API_BODY", response.body().toString())
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
                e.printStackTrace()
                Toast.makeText(
                    this@AlertsDetailsActivity,
                    e.javaClass.simpleName + "\n" + e.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun displayAlertDetails(alert: Alert) {
        val sharedPreferences = getSharedPreferences("NEC_APP", Context.MODE_PRIVATE)
        val currentUserId = sharedPreferences.getString("userId",null)
        val hasResponded = alert.responders.any{
            it._id == currentUserId
        }
        if(hasResponded){
            binding.btnRespond.text = "Alert Responded"
            binding.btnRespond.isEnabled = false
        }
        val userRole = sharedPreferences.getString("role", "citizen")
        if (userRole == "volunteer" || userRole == "admin") {
            binding.btnResolve.visibility = View.VISIBLE
        } else {
            binding.btnResolve.visibility = View.GONE
        }
        if(userRole=="citizen"){
            binding.btnRespond.visibility=View.GONE
        }else {
            binding.btnRespond.visibility=View.VISIBLE
        }
        // Show resolved by section
        if (alert.status.equals("resolved", true) && alert.resolvedBy != null) {
            binding.tvResolvedByLabel.visibility = View.VISIBLE
            binding.resolvedByLayout.visibility = View.VISIBLE
            binding.tvResolvedByName.text = alert.resolvedBy.name
            binding.tvResolvedByEmail.text = alert.resolvedBy.email
            binding.tvResolvedByName.text = "${alert.resolvedBy.name} "
            binding.tvResolvedById.text = "ID: ${alert.resolvedBy._id}"
        } else {
            binding.tvResolvedByLabel.visibility = View.GONE
            binding.resolvedByLayout.visibility = View.GONE
        }

        if (alert.status.equals("resolved", true)) {
            binding.btnResolve.isEnabled = false
            binding.btnResolve.text = "Already Resolved"
        }

        binding.btnResolve.setOnClickListener {
            if (!hasResponded) {
                Toast.makeText(
                    this,
                    "Respond to alert first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            MaterialAlertDialogBuilder(this)
                .setTitle("Resolve Alert")
                .setMessage("Are you at the alert location? Your GPS will be verified.")
                .setPositiveButton("Yes, Resolve") { _, _ ->
                    checkLocationAndResolve()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        binding.tvTitle.text = alert.title



        binding.tvStatus.text = alert.status.uppercase()
        when (alert.status.uppercase()) {

            "ACTIVE" -> {
                binding.tvStatus.setBackgroundResource(
                    R.drawable.bg_status_active
                )
            }

            "RESOLVED" -> {
                binding.tvStatus.setBackgroundResource(
                    R.drawable.bg_status_resolved
                )
            }
        }
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

            "LOW" -> {
                binding.tvSeverity.text = "LOW"
                binding.tvSeverity.setBackgroundResource(R.drawable.bg_pill_low)
            }

            "MEDIUM" -> {
                binding.tvSeverity.text = "MEDIUM"
                binding.tvSeverity.setBackgroundResource(R.drawable.bg_pill_medium)
            }

            "HIGH" -> {
                binding.tvSeverity.text = "HIGH"
                binding.tvSeverity.setBackgroundResource(R.drawable.bg_pill_high)
            }

            "CRITICAL" -> {
                binding.tvSeverity.text = "CRITICAL"
                binding.tvSeverity.setBackgroundResource(R.drawable.bg_pill_red)
            }
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
            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.tvImageLoading.text = "❌ Image not available"
                    return false
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.tvImageLoading.visibility = View.GONE
                    return false
                }
            })
            .into(binding.ivAlertImage)
        binding.tvRespondersCount.text = "${alert.responders.size} Responders"
        if (alert.status.equals("resolved", true)) {
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
                        binding.btnRespond.setText("Alert Responded")
                        fetchAlertDetails(alertId!!)
                    } else {
                        val errorMessage = try {
                            val errorBody = response.errorBody()?.string()
                            JSONObject(errorBody ?: "").getString("message")
                        } catch (e: Exception) {
                            "Something went wrong"
                        }

                        Toast.makeText(
                            this@AlertsDetailsActivity,
                            errorMessage,
                            Toast.LENGTH_SHORT
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


    private fun checkLocationAndResolve() {
        // Check permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Ask permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }
        // Get current location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                resolveAlert(location.latitude, location.longitude)
            } else {
                Toast.makeText(
                    this,
                    "Unable to get location. Please enable GPS.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun resolveAlert(lat: Double, lng: Double) {
        lifecycleScope.launch {
            try {
                val sharedPreferences = getSharedPreferences("NEC_APP", Context.MODE_PRIVATE)
                val token = sharedPreferences.getString("token", null)

                val request = ResolveRequest(
                    status = "resolved",
                    volunteerLat = lat,
                    volunteerLng = lng
                )

                val response = RetrofitInstance.api.updateAlertStatus(
                    "$token",
                    alertId!!,
                    request
                )

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@AlertsDetailsActivity,
                        "✅ Alert Resolved Successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    fetchAlertDetails(alertId!!) // refresh screen
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(
                        this@AlertsDetailsActivity,
                        "Error: $errorBody",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@AlertsDetailsActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationAndResolve()
            } else {
                Toast.makeText(
                    this,
                    "Location permission required to resolve alert",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}