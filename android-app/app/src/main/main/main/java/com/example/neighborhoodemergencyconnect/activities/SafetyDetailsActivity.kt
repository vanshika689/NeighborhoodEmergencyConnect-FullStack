package com.example.neighborhoodemergencyconnect.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.databinding.ActivitySafetyDetailsBinding
import com.example.neighborhoodemergencyconnect.models.SafetyTip
import com.example.yourpackage.repository.SafetyRepository

class SafetyDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySafetyDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySafetyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
        val category = intent.getStringExtra("category") ?: ""

        val tip = SafetyRepository.getSafetyTips()
            .firstOrNull { it.category == category }

        tip?.let {
            bindData(it)
        }
    }
    private fun bindData(tip: SafetyTip) {

        binding.txtTitle.text = tip.title
        binding.txtShortDescription.text = tip.shortDescription
        binding.txtDescription.text = tip.description
        binding.txtEmergencyNumber.text = tip.emergencyNumber

        binding.txtDos.text =
            tip.dos.joinToString("\n") { "✔️ $it" }

        binding.txtDonts.text =
            tip.donts.joinToString("\n") { "✖️ $it" }

        binding.imgCategory.setImageResource(getIcon(tip.category))

        binding.btnCallNow.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_DIAL,
                Uri.parse("tel:${tip.emergencyNumber}")
            )

            startActivity(intent)
        }
    }

    private fun getIcon(category: String): Int {

        return when (category) {

            "Fire" -> R.drawable.fireal

            "Medical" ->R.drawable.medical

            "Accident" -> R.drawable.acci

            "Crime" -> R.drawable.crime

            "Flood" ->R.drawable.flood

            "Earthquake" -> R.drawable.earth

            "Women Safety" -> R.drawable.women

            "Child Safety" -> R.drawable.child
            else -> R.drawable.other
        }
    }
}
