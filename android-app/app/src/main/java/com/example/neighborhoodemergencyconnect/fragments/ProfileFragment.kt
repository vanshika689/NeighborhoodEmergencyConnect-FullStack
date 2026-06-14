package com.example.neighborhoodemergencyconnect.fragments
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.neighborhoodemergencyconnect.activities.AboutActivity
import com.example.neighborhoodemergencyconnect.activities.DashboardActivity
import com.example.neighborhoodemergencyconnect.activities.LoginActivity
import com.example.neighborhoodemergencyconnect.activities.MyAlert
import com.example.neighborhoodemergencyconnect.activities.SettingsActivity
import com.example.neighborhoodemergencyconnect.activities.VolunteerReq
import kotlinx.coroutines.launch
import com.example.neighborhoodemergencyconnect.api.RetrofitInstance
import com.example.neighborhoodemergencyconnect.databinding.FragmentProfileBinding
import com.example.neighborhoodemergencyconnect.models.UserProfile
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnVolunteerRequest.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Volunteer Request")
                .setMessage(
                    "Are you sure you want to send a volunteer request to the admin?"
                )
                .setPositiveButton("Send Request") { _, _ ->
                    sendVolunteerRequest()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        fetchProfile()


        binding.MyAlerts.setOnClickListener {
            val intent = Intent(requireContext(), MyAlert::class.java)
            startActivity(intent)

        }

        binding.cardDashboard.setOnClickListener {
            val intent = Intent(requireContext(), DashboardActivity::class.java)
            startActivity(intent)
        }

        binding.VolunteerReqCard.setOnClickListener {
            val intent = Intent(requireContext(), VolunteerReq::class.java)
            startActivity(intent)

        }

        binding.cardAbout.setOnClickListener {
            val intent = Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }
        binding.cardSettings.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }


    }

     fun fetchProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {

                val sharedPreferences =
                    requireContext().getSharedPreferences("NEC_APP", Context.MODE_PRIVATE)

                val token = sharedPreferences.getString("token", null)
                val response = RetrofitInstance.api.getProfile("$token")

                if (response.isSuccessful) {
                    val user = response.body()?.user
                    sharedPreferences.edit().putString("role",user?.role).apply()
                    if(_binding==null) return@launch
                    if (user != null) {
                        updateUI(user)
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    e.message,
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI(user: UserProfile) {
        binding.tvName.text = user.name
        binding.tvEmail.text = user.email
        binding.chipRole.text = user.role.uppercase()
        if(user.role == "citizen"){
            binding.cardVolunteer.visibility = View.VISIBLE
        }else{
            binding.cardVolunteer.visibility = View.GONE
        }
        if(user.role == "admin"){
            binding.VolunteerReqCard.visibility = View.VISIBLE
        } else {
            binding.VolunteerReqCard.visibility = View.GONE
        }
        binding.VolunteerReq.setOnClickListener {
            val intent = Intent(requireContext(), VolunteerReq::class.java)
            startActivity(intent)
        }
        when(user.volunteerRequestStatus) {
            "none" -> {
                binding.tvVolunteerStatus.text = "Become a Volunteer"
                binding.btnVolunteerRequest.visibility = View.VISIBLE
            }

            "pending" -> {
                binding.tvVolunteerStatus.text = "🟡 Request Pending"
                binding.btnVolunteerRequest.visibility = View.GONE
            }

            "approved" -> {
                binding.tvVolunteerStatus.text = "🟢 Volunteer Approved"
                binding.btnVolunteerRequest.visibility = View.GONE
            }

            "rejected" -> {
                binding.tvVolunteerStatus.text = "🔴 Request Rejected"
                binding.btnVolunteerRequest.visibility = View.VISIBLE
                binding.btnVolunteerRequest.text = "Apply Again"
            }
        }
    }



private fun sendVolunteerRequest() {
    lifecycleScope.launch {
        try {
            val sharedPreferences =
                requireContext().getSharedPreferences("NEC_APP", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("token", null)
            val response = RetrofitInstance.api.sendVolReq("$token")
            if (response.isSuccessful) {
                Toast.makeText(
                    requireContext(),
                    "Request Sent Successfully",
                    Toast.LENGTH_SHORT
                ).show()
                fetchProfile()

            } else {
                Toast.makeText(
                    requireContext(),
                    response.errorBody()?.string() ?: "Failed to send request",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                e.message,
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }
}

}



