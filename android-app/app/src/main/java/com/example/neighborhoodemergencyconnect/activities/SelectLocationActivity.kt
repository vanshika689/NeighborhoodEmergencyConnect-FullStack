package com.example.neighborhoodemergencyconnect.activities
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.api.NominatimRetrofit
import com.example.neighborhoodemergencyconnect.databinding.ActivitySelectLocationBinding
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import com.example.neighborhoodemergencyconnect.models.SearchLocationResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices



class SelectLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectLocationBinding
    private var selectedGeoPoint: GeoPoint? = null
    private var selectedMarker: Marker? = null
    private var myLocationMarker: Marker? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var searchResults = mutableListOf<SearchLocationResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Configuration.getInstance().userAgentValue = packageName
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true) ///Enable PinchZoom, Two FingerZoom, MapDrag
        val mapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                ///This executes whenever:
                //User taps map

                if (p == null) return false
                selectedGeoPoint = p

                selectedMarker?.let {
                    //remove old marker
                    binding.mapView.overlays.remove(it)
                }

                val marker = Marker(binding.mapView)
                marker.position = p
                marker.setAnchor(
                    Marker.ANCHOR_CENTER,
                    Marker.ANCHOR_BOTTOM
                )
                marker.title = "Selected Location"
                binding.mapView.overlays.add(marker)
                selectedMarker = marker
                binding.mapView.invalidate()
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        }
        val overlay = MapEventsOverlay(mapEventsReceiver)
        binding.mapView.overlays.add(overlay)

        val mapController = binding.mapView.controller
        mapController.setZoom(19.0)
        val delhi = GeoPoint(28.6139, 77.2090)
        mapController.setCenter(delhi)
        binding.btnConfirm.setOnClickListener {
            if (selectedGeoPoint == null) {
                Toast.makeText(
                    this,
                    "Please select a location",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            lifecycleScope.launch {

                try {

                    val response =
                        NominatimRetrofit.api.reverseGeocode(
                            selectedGeoPoint!!.latitude,
                            selectedGeoPoint!!.longitude
                        )

                    if (response.isSuccessful &&
                        response.body() != null
                    ) {
                        val fullAddress = response.body()!!.display_name
                        val Shortaddress = fullAddress
                            .split(",")
                            .take(3)
                            .joinToString(",")
                        val resultIntent = Intent()

                        resultIntent.putExtra(
                            "latitude",
                            selectedGeoPoint!!.latitude
                        )

                        resultIntent.putExtra(
                            "longitude",
                            selectedGeoPoint!!.longitude
                        )

                        resultIntent.putExtra(
                            "Shortaddress",
                            Shortaddress
                        )
                        resultIntent.putExtra(
                            "fullAddress",
                            fullAddress
                        )
                        setResult(
                            RESULT_OK,
                            resultIntent
                        ) ///Returning to previous screen

                        finish()

                    } else {
                        Toast.makeText(
                            this@SelectLocationActivity,
                            "Unable to fetch address",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {

                    Toast.makeText(
                        this@SelectLocationActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        binding.btnRefresh.setOnClickListener {
            binding.btnRefresh.setOnClickListener {

                selectedMarker?.let {
                    binding.mapView.overlays.remove(it)
                }

                selectedMarker = null
                selectedGeoPoint = null

                binding.mapView.invalidate()

                Toast.makeText(
                    this,
                    "Map Refreshed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnMyLocation.setOnClickListener {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    100
                )

                return@setOnClickListener
            }

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->

                    if (location != null) {

                        val geoPoint = GeoPoint(
                            location.latitude,
                            location.longitude
                        )

                        selectedGeoPoint = geoPoint

                        selectedMarker?.let {
                            binding.mapView.overlays.remove(it)
                        }

                        val marker = Marker(binding.mapView)

                        marker.position = geoPoint

                        marker.title = "Current Location"

                        marker.setAnchor(
                            Marker.ANCHOR_CENTER,
                            Marker.ANCHOR_BOTTOM
                        )

                        binding.mapView.overlays.add(marker)

                        selectedMarker = marker

                        binding.mapView.controller.animateTo(
                            geoPoint
                        )

                        binding.mapView.controller.setZoom(19.0)

                        binding.mapView.invalidate()
                    }
                }
        }
        binding.btnMyLocation.performClick()
    }

        override fun onResume() {
            super.onResume()
            binding.mapView.onResume()
        }

        override fun onPause() {
            super.onPause()
            binding.mapView.onPause()
        }
    }


