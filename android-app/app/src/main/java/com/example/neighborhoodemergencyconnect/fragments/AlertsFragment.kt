package com.example.neighborhoodemergencyconnect.fragments
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.activities.AddAlert
import com.example.neighborhoodemergencyconnect.activities.AlertsDetailsActivity
import com.example.neighborhoodemergencyconnect.adapters.AlertAdapter
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.FragmentAlertsBinding
import com.example.neighborhoodemergencyconnect.models.Alert
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.java

class AlertsFragment : Fragment() {
    ///fragment have diff syntax of binding as that of activity
    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AlertAdapter

    private val alertsList = mutableListOf<Alert>()
    private val displayList = mutableListOf<Alert>()
    private var selectedStatus = "All"
    private var selectedCategory = "All"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = AlertAdapter(displayList) { alert ->
            val intent = Intent(requireContext(), AlertsDetailsActivity::class.java)
            intent.putExtra("alertId", alert._id)
            startActivity(intent)
        }
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = adapter
        // Inside your Fragment
        viewLifecycleOwner.lifecycleScope.launch {
            fetchAlerts()
        }
        binding.AddAlert.setOnClickListener {
            val intent = Intent(requireContext(), AddAlert::class.java)
            startActivity(intent)
        }

        val statusItems = listOf(
            "All",
            "Active",
            "Resolved"
        )
        val categoryItems = listOf(
            "All",
            "Fire",
            "Accident",
            "Crime",
            "Disaster",
            "Other"
        )

        binding.statusDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                statusItems
            )
        )

        binding.categoryDropdown.setAdapter(
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                categoryItems
            )
        )

        binding.statusDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedStatus = statusItems[position]
            applyFilters()
        }

        binding.categoryDropdown.setOnItemClickListener { _, _, position, _ ->

            selectedCategory = categoryItems[position]
            applyFilters()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvAlerts.adapter = null
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        fetchAlerts()
    }

    private fun fetchAlerts() {
       viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getAlerts()
                if (response.isSuccessful) {
                    val alerts = response.body()?.alerts
                    alertsList.clear()
                    if (alerts != null) {
                        alertsList.addAll(alerts)
                        displayList.clear()
                        displayList.addAll(alerts)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "No alerts found", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch Alerts", Toast.LENGTH_SHORT)
                        .show()
                }

            } catch(e: CancellationException){
                throw e
            }
            catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error fetching alerts: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun applyFilters() {
            val filteredAlerts = alertsList.filter { alert ->
                val statusMatch =
                    selectedStatus == "All" ||
                            alert.status.equals(selectedStatus, true)

                val categoryMatch =
                    selectedCategory == "All" ||
                            alert.title.equals(selectedCategory, true)

                statusMatch && categoryMatch
            }

        displayList.clear()
        displayList.addAll(filteredAlerts)
        adapter.notifyDataSetChanged()
        }
}
