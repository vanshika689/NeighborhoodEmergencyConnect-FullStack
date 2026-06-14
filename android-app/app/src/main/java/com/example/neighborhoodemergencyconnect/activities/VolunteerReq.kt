package com.example.neighborhoodemergencyconnect.activities

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.adapters.VolReqAdap
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.ActivityVolunteerReqBinding
import com.example.neighborhoodemergencyconnect.models.ProfileResponse
import com.example.neighborhoodemergencyconnect.models.UserProfile
import com.example.neighborhoodemergencyconnect.models.VolReq
import kotlinx.coroutines.launch

class VolunteerReq : AppCompatActivity() {
    private lateinit var Adapter: VolReqAdap
    private val volReqList = mutableListOf<UserProfile>()

    private lateinit var binding: ActivityVolunteerReqBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityVolunteerReqBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        Adapter = VolReqAdap(
            volReqList,
            onApprove = { user ->
                approveVolReq(user)
            },
            onReject = { user ->
                rejectVolReq(user)
            }
        )
        binding.rvVolunteerRequests.layoutManager =
            LinearLayoutManager(this)
        binding.rvVolunteerRequests.adapter = Adapter
        fetchVolReq()
    }

    private fun fetchVolReq() {
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences(
                    "NEC_APP",
                    MODE_PRIVATE
                ).getString("token", null)
                if (token != null) {
                    val response = RetrofitInstance.api.getVolReq("$token")
                    val body = response.body()
                    if (response.isSuccessful) {
                        val volReqs = response.body()?.requsers
                        volReqList.clear()
                        if (volReqs != null) {
                            volReqList.addAll(volReqs)
                        }
                        Adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            this@VolunteerReq,
                            "Failed to fetch requests",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@VolunteerReq,
                        "Token not Found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@VolunteerReq,
                    "Error fetching volunteer requests: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }


    private fun VolunteerReq.rejectVolReq(user: UserProfile) {
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences(
                    "NEC_APP",
                    MODE_PRIVATE
                ).getString("token", null)
                if (token != null) {
                    val response = RetrofitInstance.api.rejectVolReq("$token", user._id)
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@VolunteerReq,
                            "Volunteer Rejected",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchVolReq()
                    } else {
                        Toast.makeText(
                            this@VolunteerReq,
                            "Failed to reject volunteer request",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    Toast.makeText(this@VolunteerReq, "Token not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@VolunteerReq,
                    "Error rejecting volunteer request: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun VolunteerReq.approveVolReq(user: UserProfile) {
        lifecycleScope.launch {
            try {
                val token = getSharedPreferences(
                    "NEC_APP",
                    MODE_PRIVATE
                ).getString("token", null)
                if (token != null) {

                    val response = RetrofitInstance.api.approveVolReq("$token", user._id)
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@VolunteerReq,
                            "Volunteer request approved",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchVolReq()
                    } else {
                        Toast.makeText(
                            this@VolunteerReq,
                            "Failed to approve volunteer request",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                } else {
                    Toast.makeText(this@VolunteerReq, "Token not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@VolunteerReq,
                    "Error approving volunteer request: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    }
}

