package com.example.neighborhoodemergencyconnect.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.example.neighborhoodemergencyconnect.databinding.ItemAlertBinding
import com.example.neighborhoodemergencyconnect.models.Alert
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AlertAdapter(private val alerts: MutableList<Alert>, private val onClick: (Alert)-> Unit) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]
        holder.binding.tvType.text = alert.title
        holder.binding.tvLocation.text = alert.shortAddress
        holder.binding.tvTime.text = getRelativeTime(alert.createdAt)
        holder.binding.tvStatus.text = alert.status.uppercase() ?: "ACTIVE"

        when (alert.status.uppercase()) {

            "ACTIVE" -> {
                holder.binding.tvStatus.setBackgroundResource(
                    com.example.neighborhoodemergencyconnect.R.drawable.bg_status_active
                )
            }

            "RESOLVED" -> {
                holder.binding.tvStatus.setBackgroundResource(
                    com.example.neighborhoodemergencyconnect.R.drawable.bg_status_resolved
                )
            }
        }

        holder.binding.txtViewDetails.setOnClickListener {
            onClick(alert)
        }
        when (alert.title.trim()) {
            "Fire" -> {
                holder.binding.imgCategory.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.fireal)
                holder.binding.imgCategory.setBackgroundResource(com.example.neighborhoodemergencyconnect.R.drawable.bg_fire_circle)
            }

            "Accident" -> {
                holder.binding.imgCategory.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.acci)
                holder.binding.imgCategory.setBackgroundResource(com.example.neighborhoodemergencyconnect.R.drawable.bg_accident_circle)
            }

            "Crime" -> {
                holder.binding.imgCategory.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.crime)
                holder.binding.imgCategory.setBackgroundResource(com.example.neighborhoodemergencyconnect.R.drawable.bg_crime_circle)
            }

            "Disaster" -> {
                holder.binding.imgCategory.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.dis)
                holder.binding.imgCategory.setBackgroundResource(com.example.neighborhoodemergencyconnect.R.drawable.bg_disaster_circle)
            }

            else -> {
                holder.binding.imgCategory.setImageResource(com.example.neighborhoodemergencyconnect.R.drawable.other)
                holder.binding.imgCategory.setBackgroundResource(com.example.neighborhoodemergencyconnect.R.drawable.bg_other_circle)
            }
        }
        if (alert.aiAnalysis?.isFake == true) {

            holder.binding.tvFakeStatus.text = "✕ FAKE"

            holder.binding.tvFakeStatus.setBackgroundResource(
                com.example.neighborhoodemergencyconnect.R.drawable.bg_pill_fake

            )
            holder.binding.tvSeverity.visibility = View.GONE

            holder.binding.severityStrip.setBackgroundColor(
                android.graphics.Color.parseColor("#991B1B")
            )
        } else {
            holder.binding.tvFakeStatus.text = "✓ REAL"

            holder.binding.tvFakeStatus.setBackgroundResource(
                com.example.neighborhoodemergencyconnect.R.drawable.bg_pill_green
            )

            holder.binding.tvSeverity.visibility = android.view.View.VISIBLE
        }
        when (alert.aiAnalysis?.severity?.trim()) {
            "LOW" -> {
                holder.binding.tvSeverity.text = "LOW"

                holder.binding.tvSeverity.setBackgroundResource(
                    com.example.neighborhoodemergencyconnect.R.drawable.bg_pill_low
                )

                holder.binding.severityStrip.setBackgroundColor(
                    android.graphics.Color.parseColor("#22C55E")
                )
            }

            "MEDIUM" -> {
                holder.binding.tvSeverity.text = "MEDIUM"

                holder.binding.tvSeverity.setBackgroundResource(
                    com.example.neighborhoodemergencyconnect.R.drawable.bg_pill_medium
                )

                holder.binding.severityStrip.setBackgroundColor(
                    android.graphics.Color.parseColor("#EAB308")
                )
            }

            "HIGH" -> {

                holder.binding.tvSeverity.text = "HIGH"

                holder.binding.tvSeverity.setBackgroundResource(
                    com.example.neighborhoodemergencyconnect.R.drawable.bg_pill_high
                )

                holder.binding.severityStrip.setBackgroundColor(
                    android.graphics.Color.parseColor("#F97316")
                )
            }

            "CRITICAL" -> {
                holder.binding.tvSeverity.text = "CRITICAL"
                holder.binding.tvSeverity.setBackgroundResource(
                    com.example.neighborhoodemergencyconnect.R.drawable.bg_pill_red
                )

                holder.binding.severityStrip.setBackgroundColor(
                    android.graphics.Color.parseColor("#DC2626")
                )
            }

            else -> {

                holder.binding.tvSeverity.text = "MEDIUM"

                holder.binding.tvSeverity.setBackgroundResource(
                    com.example.neighborhoodemergencyconnect.R.drawable.bg_pill_medium
                )

                holder.binding.severityStrip.setBackgroundColor(
                    android.graphics.Color.parseColor("#EAB308")
                )
            }
        }

    }


    override fun getItemCount(): Int {
        return alerts.size
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



}


