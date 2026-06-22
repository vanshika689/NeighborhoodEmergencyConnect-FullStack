package com.example.neighborhoodemergencyconnect.fragments
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.launch
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import android.Manifest
import androidx.core.widget.addTextChangedListener
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.neighborhoodemergencyconnect.activities.AlertsDetailsActivity
import com.example.neighborhoodemergencyconnect.models.Alert

class MapFragment : Fragment() {
    private var myLocationMarker: Marker? = null

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var allAlerts = mutableListOf<Alert>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )

        binding.mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        val mapController = binding.mapView.controller
        mapController.setZoom(19.0)
        val delhi = GeoPoint(28.6139, 77.2090)
        mapController.setCenter(delhi)
        loadAlertsOnMap()
        binding.btnRefresh.setOnClickListener {
            loadAlertsOnMap()
            Toast.makeText(requireContext(), "Alerts Refreshed", Toast.LENGTH_SHORT).show()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.btnMyLocation.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    100
                )
                return@setOnClickListener
            }
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val geoPoint = GeoPoint(location.latitude, location.longitude)
                    myLocationMarker?.let{
                        binding.mapView.overlays.remove(it)
                    }
                    val marker = Marker(binding.mapView)
                    marker.position = geoPoint
                    marker.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )
                    marker.title = "Your Location"
                    marker.icon = ContextCompat.getDrawable(requireContext(), com.example.neighborhoodemergencyconnect.R.drawable.loc)
                    binding.mapView.overlays.add(marker)
                    myLocationMarker = marker
                    binding.mapView.invalidate()
                    binding.mapView.controller.animateTo(geoPoint)
                    binding.mapView.controller.setZoom(19.0)
                }
            }

        }
        binding.chipAll.setOnClickListener {
            filterMarkers("All")
        }
        binding.chipFire.setOnClickListener {
            filterMarkers("Fire")
        }
        binding.chipAccident.setOnClickListener {
            filterMarkers("Accident")
        }
        binding.chipDisaster.setOnClickListener {
            filterMarkers("Disaster")
        }
        binding.chipCrime.setOnClickListener {
            filterMarkers("Crime")
        }
        binding.etSearch.addTextChangedListener {
            val query = it.toString()
            searchAlerts(query)
        }
        binding.mapView.setMultiTouchControls(true)
        binding.mapView.isTilesScaledToDpi = true
    }


    override fun onResume() {
        super.onResume()
        loadAlertsOnMap()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroyView() {
        binding.mapView.overlays.clear()
        _binding = null
        super.onDestroyView()

    }

    private fun loadAlertsOnMap() {
        if (_binding == null || !isAdded) return

        viewLifecycleOwner.lifecycleScope.launch {
            ///Background Coroutine
            try {
                val currentBinding = _binding ?: return@launch
                val currentContext = context ?: return@launch

                binding.mapView.overlays.clear()
                val response = RetrofitInstance.api.getAlerts()
                if (response.isSuccessful && response.body() != null) {
                    val alerts = response.body()!!.alerts
                    allAlerts.clear()
                    allAlerts.addAll(alerts)
                    currentBinding.mapView.overlays.clear()
                    for (alert in alerts) {
                        createMarker(alert)

                    }
                    currentBinding.mapView.invalidate()  //refresh screen MapUi

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to load alerts",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error loading alerts: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    override fun onDetach() {
        super.onDetach()
        _binding = null
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


    private fun filterMarkers(category: String) {
        if (_binding == null || !isAdded) return
        val currentBinding = _binding ?: return // Safety check
        currentBinding.mapView.overlays.clear()

        val filteredAlerts =
            if (category == "All")
                allAlerts
            else
                allAlerts.filter {
                    it.title.equals(category, true)
                }
        if (filteredAlerts.isEmpty()) {
            binding.cardSelectedAlert.visibility = View.GONE
            Toast.makeText(requireContext(), "No Alerts Found", Toast.LENGTH_SHORT).show()
            return
        }

        for (alert in filteredAlerts) {
            createMarker(alert)
        }
        currentBinding.mapView.invalidate()
    }

    private fun createMarker(alert: Alert) {
        if (_binding == null || !isAdded) return
        val marker = Marker(binding.mapView)
        marker.position = GeoPoint(
            alert.latitude,
            alert.longitude
        )
        marker.title = alert.title
        marker.snippet = alert.shortAddress

        marker.setAnchor(
            Marker.ANCHOR_CENTER,
            Marker.ANCHOR_BOTTOM
        )

        marker.setOnMarkerClickListener { _, _ ->
            binding.cardSelectedAlert.visibility = View.VISIBLE
            binding.tvAlertTitle.text = alert.title
            binding.tvAlertLocation.text = alert.shortAddress
            binding.tvAlertTime.text =
                getRelativeTime(alert.createdAt)

            when (alert.title.trim()) {
                "Fire" -> binding.imgAlert.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.fireal)
                "Accident" -> binding.imgAlert.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.acci)
                "Disaster" -> binding.imgAlert.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.dis)
                "Crime" -> binding.imgAlert.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.crime)
                else -> binding.imgAlert.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.other)
            }

            binding.btnViewDetails.setOnClickListener {
                val intent = Intent(
                    requireContext(),
                    AlertsDetailsActivity::class.java
                )

                intent.putExtra(
                    "alertId",
                    alert._id
                )

                startActivity(intent)
            }
            true
        }

        binding.mapView.overlays.add(marker)
    }


    private fun searchAlerts(query: String) {
        if (_binding == null || !isAdded) return
        binding.mapView.overlays.clear()
        val filteredAlerts = allAlerts.filter {
            it.fullAddress.contains(query, true)
        }
        for (alert in filteredAlerts) {
            createMarker(alert)
        }
        binding.mapView.invalidate()
    }
}
