package com.example.neighborhoodemergencyconnect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.neighborhoodemergencyconnect.R
import com.example.neighborhoodemergencyconnect.databinding.ItemSafetyTipBinding
import com.example.neighborhoodemergencyconnect.models.SafetyTip

class SafetyAdapter(
    private val safetyTips: List<SafetyTip>,
    private val onClick: (SafetyTip) -> Unit
) : RecyclerView.Adapter<SafetyAdapter.SafetyViewHolder>() {

    inner class SafetyViewHolder(
        val binding: ItemSafetyTipBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SafetyViewHolder {

        val binding = ItemSafetyTipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return SafetyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SafetyViewHolder, position: Int) {

        val tip = safetyTips[position]

        with(holder.binding) {

            txtTitle.text = tip.title
            txtSubtitle.text = tip.shortDescription

            imgCategory.setImageResource(getCategoryIcon(tip.category))

            root.setOnClickListener {
                onClick(tip)
            }
        }
    }

    override fun getItemCount(): Int = safetyTips.size

    private fun getCategoryIcon(category: String): Int {

        return when (category) {

            "Fire" -> R.drawable.fireal

            "Medical" -> R.drawable.medical

            "Accident" -> R.drawable.acci

            "Crime" -> R.drawable.crime

            "Flood" -> R.drawable.flood

            "Earthquake" -> R.drawable.earth

            "Women Safety" -> R.drawable.women

            "Child Safety" -> R.drawable.child

            else -> R.drawable.other
        }
    }
}