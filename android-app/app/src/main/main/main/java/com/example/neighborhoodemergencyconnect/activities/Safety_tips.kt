package com.example.neighborhoodemergencyconnect.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.neighborhoodemergencyconnect.adapter.SafetyAdapter
import com.example.neighborhoodemergencyconnect.databinding.ActivitySafetyTipsBinding
import com.example.yourpackage.repository.SafetyRepository

class Safety_tips : AppCompatActivity() {
    lateinit var binding: ActivitySafetyTipsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySafetyTipsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        val safetyTips = SafetyRepository.getSafetyTips()

        binding.rvSafetyTips.layoutManager = LinearLayoutManager(this)

        binding.rvSafetyTips.adapter = SafetyAdapter(safetyTips) { tip ->
            val intent = Intent(this, SafetyDetailsActivity::class.java)

            intent.putExtra("category", tip.category)
            startActivity(intent)

        }

    }
}
